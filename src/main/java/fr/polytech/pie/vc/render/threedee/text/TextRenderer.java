package fr.polytech.pie.vc.render.threedee.text;

import fr.polytech.pie.vc.render.threedee.opengl.VertexArray;
import fr.polytech.pie.vc.render.threedee.opengl.VertexBuffer;
import fr.polytech.pie.vc.render.threedee.opengl.shader.TextShader;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.util.freetype.FreeType.*;

public class TextRenderer {
    private long ftLibrary;

    private final TextShader shader = new TextShader();
    private VertexArray textVAO;

    private final List<List<TextChar>> glyphs = new ArrayList<>();

    public void init() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pLibrary = stack.mallocPointer(1);

            int err = FT_Init_FreeType(pLibrary);
            if (err != 0) {
                throw new RuntimeException("Failed to init FreeType. Error code: " + err);
            }
            ftLibrary = pLibrary.get(0);
        }

        shader.load();

        textVAO = new VertexArray();

        VertexBuffer textBuffer = new VertexBuffer(
                4 * 6 * 4, // sizeof(float) * 4 floats per vertex * 6 vertices (see TextShader)
                GL30.GL_DYNAMIC_DRAW
        );
        textBuffer.addVertexAttribPointer(0, 4, GL11.GL_FLOAT, 4, 0);

        textVAO.bind();
        textVAO.bindVertexBuffer(textBuffer);
        textVAO.unbind();
    }

    // Suppress resource warning for autocloseable FT_Bitmap objects as they are served as a view to the internal freetype data and are not meant to be destroyed manually.
    @SuppressWarnings("resource")
    public int loadFont(String fontPath, int fontSize) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pFace = stack.mallocPointer(1);
            int err = FT_New_Face(ftLibrary, fontPath, 0, pFace);
            if (err != 0) {
                throw new RuntimeException("Failed to load font. Error code: " + err);
            }

            long faceAddress = pFace.get(0);
            FT_Face face = FT_Face.create(faceAddress);

            FT_Set_Pixel_Sizes(face, 0, fontSize);

            List<TextChar> glyphList = new ArrayList<>();

            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            for (int i = 0; i < 128; i++) {
                err = FT_Load_Char(face, (char) i, FT_LOAD_RENDER);
                if (err != 0) {
                    throw new RuntimeException("Failed to load character. Error code: " + err);
                }

                int texture = GL11.glGenTextures();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

                FT_GlyphSlot glyphSlot = Objects.requireNonNull(face.glyph());

                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                GL11.glTexImage2D(
                        GL11.GL_TEXTURE_2D,
                        0,
                        GL11.GL_RED,
                        glyphSlot.bitmap().width(),
                        glyphSlot.bitmap().rows(),
                        0,
                        GL11.GL_RED,
                        GL11.GL_UNSIGNED_BYTE,
                        // We don't care about the capacity since opengl will only pick up the buffer address from this field
                        glyphSlot.bitmap().buffer(0)
                );

                TextChar textChar = new TextChar(
                        texture,
                        new Vector2i(
                                glyphSlot.bitmap().width(),
                                glyphSlot.bitmap().rows()
                        ),
                        new Vector2i(
                                glyphSlot.bitmap_left(),
                                glyphSlot.bitmap_top()
                        ),
                        (int) glyphSlot.advance().x()
                );

                glyphList.add(textChar);
            }
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

            FT_Done_Face(face);

            glyphs.add(glyphList);

            return glyphs.size() - 1;
        }
    }

    public void destroy() {
        textVAO.destroy();
        shader.destroy();
        for (List<TextChar> fontGlyphs : glyphs) {
            for (TextChar glyph : fontGlyphs) {
                glyph.destroy();
            }
        }
        if (ftLibrary != 0) {
            FT_Done_FreeType(ftLibrary);
        }
    }

    public void renderText(int fontID, Vector2i position, Vector2i screenSize, float scale, Vector3f color, String text) {
        if (fontID < 0 || fontID >= glyphs.size()) {
            throw new IllegalArgumentException("Invalid font ID");
        }

        List<TextChar> glyphList = glyphs.get(fontID);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        shader.start();
        shader.setProjectionMatrix(new Matrix4f().ortho(0.0F, screenSize.x, 0.0F, screenSize.y, -1, 1));
        shader.setColor(color);
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        textVAO.bind();

        for (char c : text.toCharArray()) {
            if (c >= 128) {
                continue; // Skip characters outside the ASCII range
            }

            TextChar ch = glyphList.get(c);

            float x = position.x + ch.bearing().x() * scale;
            float y = position.y - (ch.size().y - ch.bearing().y()) * scale;

            float w = ch.size().x() * scale;
            float h = ch.size().y() * scale;

            float[] vertices = new float[]{
                    x, y + h, 0.0f, 0.0f,
                    x, y, 0.0f, 1.0f,
                    x + w, y, 1.0f, 1.0f,

                    x, y + h, 0.0f, 0.0f,
                    x + w, y, 1.0f, 1.0f,
                    x + w, y + h, 1.0f, 0.0f
            };

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, ch.textureID());

            VertexBuffer textBuffer = textVAO.getVertexBuffer(0);
            textBuffer.storeData(vertices);

            GL11.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);
            position.x += (int) ((ch.advance() >> 6) * scale); // Bitshift by 6 to convert from 1/64th of a pixel to pixel
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        textVAO.unbind();
        shader.stop();
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static String getResourceFontPath(String filename) {
        URL resource = TextRenderer.class.getResource("/fonts/" + filename);
        if (resource == null) {
            throw new RuntimeException("Font file not found");
        }

        return resource.getPath();
    }
}
