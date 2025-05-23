# Tetris en 2D et 3D

Ce projet est une implémentation du jeu Tetris en utilisant le modèle MVC (Modèle-Vue-Contrôleur). Il inclut des fonctionnalités avancées telles que l'intelligence artificielle, le rendu 3D, et bien plus.

## Fonctionnalités

- **Jeu Tetris** : Une implémentation complète du jeu Tetris avec les rotations wallkick et T-spin.
- **Architecture MVC** : Utilisation du modèle Modèle-Vue-Contrôleur pour une séparation claire des responsabilités.
- **Abstraction entre 2D et 3D** : Une abstraction maximale entre les rendus 2D et 3D.
- **Rendu 3D bas niveau** : Utilisation d'OpenGL pour un rendu 3D (bas niveau), et implémentation de deux caméras (caméra libre et caméra dirigée).
- **Intelligence Artificielle** : Une IA qui calcule le meilleur coup à chaque fois avec une récursion de profondeur 2 (la prochaine pièce est prise en compte).
- **IA en 3D** : L'IA fonctionne également en mode 3D.
- **Entraînement de l'IA** : Entraînement de l'IA fait maison avec un algorithme génétique.
- **Parallélisation des calculs de l'IA** : Utilisation du nombre de threads le plus efficace pour paralléliser les calculs de l'IA (WorkStealingPool).
- **Musique de Tetris** : Intégration de la musique de Tetris.
- **Stockage du meilleur score** : Sauvegarde et affichage du meilleur score.
- **Prévisualisation de la chute de la pièce** : Visualisation de la chute de la pièce avant de la placer.
- **Prévisualisation de la prochaine pièce** : Affichage de la prochaine pièce à venir.

## Diagramme de Classes

```mermaid
classDiagram
direction TB
	namespace model {
        class BoundingBox {
	        + BoundingBox()
	        ~ update(Vector3f) void
        }
        class Piece2D {
	        PieceColor[][] piece2d
	        int width
	        int height
	        + Piece2D(boolean[][], TetrisVector)
	        + clone() Piece2D
	        + rotate2d(Predicate~Piece~) void
        }
        class Grid2D {
	        int holes
	        + Grid2D(TetrisVector)
	        + freezePiece(Piece) void
	        + getPiecesPossibilities(Piece) Set~Piece~
	        - recalculateAllCaches() void
	        + removePiece(Piece) void
	        + copy() Grid
	        + checkCollision(Piece) boolean
	        + clearFullLines() int
	        + clearFullLines(boolean) int
	        + getHeightOfColumn2D(int) int
	        - updateCache(int) void
	        + getValue(TetrisVector) PieceColor
	        + setValue(TetrisVector, PieceColor) void
        }
        class Piece {
	        # PieceColor color
	        # TetrisVector position
	        int width
	        PieceColor color
	        TetrisVector position
	        int height
	        + Piece(TetrisVector)
	        + clone() Piece
	        + translate(TetrisVector) void
        }
        class Grid3D {
	        int holes
	        + Grid3D(TetrisVector)
	        - updateCaches(int, int) void
	        + setValue(TetrisVector, PieceColor) void
	        + clearFullLines(boolean) int
	        + getPiecesPossibilities(Piece) Set~Piece~
	        + getHeightOfColumn3D(int, int) int
	        + freezePiece(Piece) void
	        + checkCollision(Piece) boolean
	        - recalculateAllCaches() void
	        + clearFullLines() int
	        + copy() Grid
	        + getValue(TetrisVector) PieceColor
	        + removePiece(Piece) void
        }
        class Model {
	        - int difficulty
	        - Game game
	        int difficulty
	        Game game
	        boolean gameOver
	        int droppedYCurrentPiece
	        + Model()
	        + rotateCurrentPiece2D() void
	        + resetGame() void
	        - loadHighScore() void
	        + getHighScore(boolean) long
	        + rotateCurrentPiece3D(RotationAxis, boolean) void
	        + dropCurrentPiece() void
	        + changeRenderingMode(boolean) void
	        + setHighScore(long, boolean) void
	        + rotateCurrentPiece3D(RotationAxis) void
	        + startGame(boolean) void
	        + stopGame() void
	        + saveHighScore() void
	        + cleanup() void
	        + runAi() void
	        + translateCurrentPiece(TetrisVector) void
        }
        class PieceColor {
	        Vector3f vector
	        Color color
	        + PieceColor()
	        + valueOf(String) PieceColor
	        + values() PieceColor[]
        }
        class TetrisVector {
	        - int[] vector
	        - int size
	        int size
	        int x
	        int y
	        int z
	        int[] vector
	        + TetrisVector(TetrisVector)
	        + TetrisVector(int)
	        + TetrisVector(int[])
	        + equals(Object) boolean
	        + add(TetrisVector) void
	        + subtract(TetrisVector) void
	        + toVector3f() Vector3f
	        + toString() String
	        + hashCode() int
        }
        class Game {
	        - Grid grid
	        - boolean gameOver
	        - long score
	        - Piece nextPiece
	        boolean 3D
	        long score
	        Grid grid
	        Piece nextPiece
	        boolean gameOver
	        Piece currentPiece
	        boolean renderingMode
	        + Game(boolean, int, Runnable)
	        - freeze() void
	        - updateScore(int) void
	        + resetGame() void
	        + translateCurrentPiece(TetrisVector) void
	        + rotateCurrentPiece() void
	        - generateNewPiece() void
	        + runAi() void
	        + cleanup() void
	        + rotateCurrentPiece3D(RotationAxis, boolean) void
        }
        class PieceGenerator {
	        + PieceGenerator()
	        - rotate2DPieceRandomly(Piece2D) void
	        + generate3DPiece(int, int, int) Piece3D
	        - rotate3DPieceRandomly(Piece3D) void
	        + generatePiece2D(int, int) Piece2D
        }
        class GeneticTrainer {
	        int adaptivePieceCount
	        + GeneticTrainer(int, int, boolean)
	        - createOffspring() List~Individual~
	        - mutate(Individual) void
	        - replaceWorstIndividuals(List~Individual~) void
	        - printHelp() void
	        - runGame(double[], int) int
	        - crossover(Individual, Individual) Individual
	        + main(String[]) void
	        - initializePopulation() void
	        - evaluateIndividual(Individual) int
	        - sphericalInterpolation(Individual, Individual) Individual
	        - checkImprovement() void
	        - saveParametersToFile(double[], String) void
	        - evaluatePopulation() void
	        - tournamentSelection() Individual
	        - parseIntArg(String, int) int
	        - resetStagnantPopulation() void
	        + train(int) double[]
	        - updateAdaptiveParameters(int, int) void
        }
        class Piece3D {
	        int width
	        PieceColor[][][] piece3d
	        int depth
	        int height
	        + Piece3D(boolean[][][], TetrisVector)
	        - calculateRotatedBounds(Matrix3f, Vector3f, int, int, int) BoundingBox
	        - createRotatedPiece(Matrix3f, Vector3f, BoundingBox, int, int, int) PieceColor[][][]
	        - applyRotation(Matrix3f) void
	        - isValidIndex(int, int, int, int, int, int) boolean
	        - calculateCenter(int, int, int) Vector3f
	        + rotate3D(RotationAxis, Predicate~Piece~, boolean) void
	        + clone() Piece3D
        }
        class Ai {
	        + Ai(Grid, AIParameters)
	        + shutdown() void
	        - getCallables(Piece, Set~Piece~) List~Callable~PieceMoveScore~~
	        + makeMove(Piece, Piece) void
	        - getScore(Grid) double
	        - getBumpiness(Grid) int
	        - getHeights(Grid) int
        }
        class Grid {
	        int width
	        int depth
	        int holes
	        int height
	        + Grid(TetrisVector)
	        + freezePiece(Piece) void
	        + clearFullLines(boolean) int
	        + getPiecesPossibilities(Piece) Set~Piece~
	        + isOutOfBounds(TetrisVector) boolean
	        + checkCollision(Piece) boolean
	        + getValue(TetrisVector) PieceColor
	        + clearFullLines() int
	        + removePiece(Piece) void
	        + copy() Grid
	        + create(TetrisVector, boolean) Grid
        }
        class PieceMoveScore {
	        - PieceMoveScore(Piece, double)
	        + score() double
	        + piece() Piece
        }
        class Individual {
	        + Individual(double[])
	        + compareTo(Individual) int
	        - normalize(double[]) double[]
        }
        class AIParameters {
	        + AIParameters(double, double, double, double)
	        + holesWeight() double
	        + linesWeight() double
	        + heightWeight() double
	        + bumpinessWeight() double
        }
        class RotationAxis {
	        + RotationAxis()
	        + values() RotationAxis[]
	        + valueOf(String) RotationAxis
        }
	}
	namespace vc {
        class OpenGLRenderer {
	        + OpenGLRenderer()
	        + destroy() void
	        + updateCubes(List~Cube~) void
	        + render(long, Vector2i, Matrix4f, Matrix4f, long, boolean, float) void
	        + init() void
	        + drawCubes(Matrix4f, Matrix4f, List~Cube~, boolean) void
	        + renderPlayingBox(Matrix4f, Matrix4f, Vector3f, Vector3f) void
	        + renderScore(Vector2i, long) void
        }
        class Renderer3D {
	        + Renderer3D(VueController)
	        + initialize() void
	        - handleKeyboardInput(float, boolean[]) void
	        + update(Grid, Piece, Piece, long, boolean) void
	        + cleanup() void
	        + loop() LoopStatus
        }
        class CameraController {
	        - Vector3f directedCamTarget
	        - boolean isFreeCam
	        Vector3f directedCamTarget
	        boolean isFreeCam
	        DirectedCamera directedCam
	        Matrix4f currentViewMatrix
	        float aspectRatio
	        Matrix4f currentProjectionMatrix
	        + CameraController(boolean, float)
	        + switchToDirectedCam() void
	        + handleMouseWheel(float, double) void
	        + switchToFreeCam() void
	        + handleKeyboardInput(float, boolean[], boolean[]) void
	        + handleMouseInput(double, double) void
        }
        class TextChar {
	        + TextChar(int, Vector2i, Vector2i, int)
	        + advance() int
	        + destroy() void
	        + size() Vector2i
	        + bearing() Vector2i
	        + textureID() int
        }
        class VertexBuffer {
	        + VertexBuffer(int, int)
	        - VertexBuffer()
	        + VertexBuffer(float[], int)
	        + bind() void
	        + bindVertexAttribs() void
	        + unbind() void
	        + addVertexAttribPointer(int, int, int, int, int) void
	        + storeData(float[]) void
	        + destroy() void
        }
        class TextRenderer {
	        + TextRenderer()
	        + init() void
	        + renderText(int, Vector2i, Vector2i, float, Vector3f, String) void
	        + loadFont(Path, int) int
	        + getResourceFontPath(String) Path
	        + destroy() void
        }
        class Renderer2D {
	        + Renderer2D(VueController)
	        - drawGridCell(TetrisVector, PieceColor) void
	        + loop() LoopStatus
	        - drawGrid(int, int, JPanel[][], JPanel) void
	        - performAction(KeyAction) void
	        - updateCountdownLabel() void
	        - createScorePanel() JPanel
	        - initializeNextPiecePanel() void
	        + update(Grid, Piece, Piece, long, boolean) void
	        - clearGrid() void
	        - initializeGridPanel() void
	        - setupScheduler() void
	        - setupGameOverPanel() void
	        + cleanup() void
	        - resetGameOver() void
	        - createScoreLabel() JLabel
	        - clearNextPieceGrid() void
	        - drawNextPiece(Piece2D) void
	        - drawCurrentPiece(Piece2D) void
	        - setupKeyboard() void
	        - setupFrame() void
	        - createSidePanel() JPanel
	        - showGameOver() void
	        - drawFrozenPieces(Grid) void
	        + initialize() void
        }
        class RendererType {
	        + RendererType()
	        + values() RendererType[]
	        + valueOf(String) RendererType
        }
        class RenderUtils {
	        int[] boxWireframeIndices
	        float[] cubeNormals
	        int[] cubeIndices
	        + RenderUtils()
	        + initWireframeBoxVAO(Vector3f) VertexArray
	        - getBoxPositions(Vector3f) float[]
	        + initCubeVAO() VertexArray
        }
        class KeyAction {
	        - KeyAction(int, int, boolean, double)
	        - KeyAction(int, int, boolean)
	        + values() KeyAction[]
	        + valueOf(String) KeyAction
        }
        class TetrisCubePanel {
	        PieceColor piece
	        ~ TetrisCubePanel()
        }
        class MenuRenderer {
	        + MenuRenderer(long, long, VueController)
	        - createControlsTextArea() JTextPane
	        - createButtonPanel() JPanel
	        - createGameControlsPanel(JPanel, JPanel) JPanel
	        + update(Grid, Piece, Piece, long, boolean) void
	        + cleanup() void
	        + initialize() void
	        - createLevelPanel() JPanel
	        - createHighScorePanel() JPanel
	        + loop() LoopStatus
	        - createBottomPanel() JPanel
	        - createCenterPanel(JPanel) JPanel
	        - createTitleLabel() JLabel
        }
        class VueController {
	        - Model model
	        int difficulty
	        Model model
	        + VueController(Model)
	        + update(Observable, Object) void
	        ~ stopGame() void
	        + loop() void
	        - switchRenderer(RendererType) void
	        + cleanup() void
	        ~ startGame(boolean) void
        }
        class Cube {
	        + Cube(Vector3f, Vector3f)
	        + position() Vector3f
	        + color() Vector3f
        }
        class VertexAttribPointer {
	        + VertexAttribPointer(int, int, int, int, int)
	        + type() int
	        + index() int
	        + stride() int
	        + offset() int
	        + size() int
        }
        class TextShader {
	        Matrix4f projectionMatrix
	        Vector3f color
	        + TextShader()
	        # getAllUniformLocations() void
	        + load() void
        }
        class LoopStatus {
	        + LoopStatus()
	        + valueOf(String) LoopStatus
	        + values() LoopStatus[]
        }
        class Renderer {
	        + cleanup() void
	        + loop() LoopStatus
	        + initialize() void
	        + update(Grid, Piece, Piece, long, boolean) void
        }
        class VertexArray {
	        int vertexCount
	        + VertexArray(ElementBuffer)
	        + VertexArray()
	        + unbind() void
	        + getVertexBuffer(int) VertexBuffer
	        + bind() void
	        - bindElementArrayBuffer() void
	        + destroy() void
	        + bindVertexBuffer(VertexBuffer) void
        }
        class FreeCamCamera {
	        - Matrix4f? projectionMatrix
	        - float pitch
	        - Vector3f pos
	        - Matrix4f? viewMatrix
	        + float aspectRatio
	        - float yaw
	        Matrix4f viewMatrix
	        float yaw
	        Matrix4f projectionMatrix
	        Vector3f pos
	        float aspectRatio
	        float pitch
	        + FreeCamCamera()
	        + FreeCamCamera(float)
	        + moveForward(float) void
	        + addYaw(float) void
	        + lookAt(Vector3f) void
	        + moveDown(float) void
	        + moveLeft(float) void
	        + moveRight(float) void
	        + moveBackwards(float) void
	        + moveUp(float) void
	        + addPitch(float) void
        }
        class DirectedCamera {
	        - float distanceFromTarget
	        - Matrix4f? viewMatrix
	        - float aspectRatio
	        - Vector3f target
	        - float verticalAngle
	        - float horizontalAngle
	        - Matrix4f? projectionMatrix
	        float distanceFromTarget
	        Vector3f target
	        float aspectRatio
	        float verticalAngle
	        Matrix4f viewMatrix
	        Matrix4f projectionMatrix
	        float horizontalAngle
	        + DirectedCamera(Vector3f)
	        + DirectedCamera(float)
	        + addDistanceFromTarget(float) void
	        + computePos() Vector3f
	        + addHorizontalAngle(float) void
	        + addVerticalAngle(float) void
        }
        class ElementBuffer {
	        - int indicesCount
	        int indicesCount
	        + ElementBuffer(int[])
	        + destroy() void
	        + unbind() void
	        + bind() void
        }
        class ShaderProgram {
	        + ShaderProgram()
	        + load() void
	        + loadMat4(int, Matrix4f) void
	        # getAllUniformLocations() void
	        + loadFloat(int, float) void
	        + loadProgram(String, String) void
	        + loadInt(int, int) void
	        - loadShader(String, int) int
	        # getUniformLocation(String) int
	        + stop() void
	        + loadVector(int, Vector3f) void
	        + destroy() void
	        + start() void
        }
        class SimpleShader {
	        Matrix4f viewMatrix
	        Matrix4f projectionMatrix
	        Vector3f color
	        Vector3f pos
	        + SimpleShader()
	        + load() void
	        # getAllUniformLocations() void
        }
        class CubeShader {
	        Matrix4f viewMatrix
	        Vector3f lightPosition
	        Matrix4f projectionMatrix
	        Vector3f color
	        float brightness
	        Vector3f pos
	        + CubeShader()
	        # getAllUniformLocations() void
	        + load() void
        }
	}
    class Consts {
	    + Consts()
    }
    class Main {
	    + Main()
	    + main(String[]) void
    }

	<<enumeration>> PieceColor
	<<enumeration>> RendererType
	<<enumeration>> KeyAction
	<<enumeration>> LoopStatus
	<<Interface>> Renderer
	<<enumeration>> RotationAxis

    Ai "1" *--> "params 1" AIParameters
    Ai "1" *--> "grid 1" Grid
    Ai ..> PieceMoveScore : «create»
    Piece3D --> BoundingBox
    CameraController ..> DirectedCamera : «create»
    CameraController "1" *--> "directedCamera 1" DirectedCamera
    CameraController ..> FreeCamCamera : «create»
    CameraController "1" *--> "freeCam 1" FreeCamCamera
    CubeShader --> ShaderProgram
    Game ..> Ai : «create»
    Game "1" *--> "ai 1" Ai
    Game "1" *--> "grid 1" Grid
    Game "1" *--> "piece 1" Piece
    Game ..> TetrisVector : «create»
    GeneticTrainer ..> AIParameters : «create»
    GeneticTrainer ..> Ai : «create»
    GeneticTrainer ..> Grid2D : «create»
    GeneticTrainer ..> Grid3D : «create»
    GeneticTrainer "1" *--> "population *" Individual
    GeneticTrainer ..> Individual : «create»
    GeneticTrainer ..> TetrisVector : «create»
    Grid ..> Grid2D : «create»
    Grid ..> Grid3D : «create»
    Grid "1" *--> "size 1" TetrisVector
    Grid2D --> Grid
    Grid2D "1" *--> "grid *" PieceColor
    Grid2D ..> PieceColor : «create»
    Grid2D ..> TetrisVector : «create»
    Grid3D --> Grid
    Grid3D "1" *--> "grid *" PieceColor
    Grid3D ..> PieceColor : «create»
    Grid3D ..> TetrisVector : «create»
    GeneticTrainer --> Individual
    Renderer2D --> KeyAction
    Main ..> Model : «create»
    Main ..> VueController : «create»
    MenuRenderer "1" *--> "nextLoopStatus 1" LoopStatus
    MenuRenderer ..> Renderer
    MenuRenderer "1" *--> "vueController 1" VueController
    Model "1" *--> "game 1" Game
    Model ..> Game : «create»
    OpenGLRenderer "1" *--> "cubes *" Cube
    OpenGLRenderer "1" *--> "cubeShader 1" CubeShader
    OpenGLRenderer ..> CubeShader : «create»
    OpenGLRenderer ..> SimpleShader : «create»
    OpenGLRenderer "1" *--> "simpleShader 1" SimpleShader
    OpenGLRenderer ..> TextRenderer : «create»
    OpenGLRenderer "1" *--> "textRenderer 1" TextRenderer
    OpenGLRenderer "1" *--> "cubeVao 1" VertexArray
    Piece "1" *--> "color 1" PieceColor
    Piece "1" *--> "position 1" TetrisVector
    Piece ..> TetrisVector : «create»
    Piece2D --> Piece
    Piece2D ..> PieceColor : «create»
    Piece2D "1" *--> "pieceColor *" PieceColor
    Piece2D ..> TetrisVector : «create»
    Piece3D ..> BoundingBox : «create»
    Piece3D --> Piece
    Piece3D ..> PieceColor : «create»
    Piece3D "1" *--> "pieceColor *" PieceColor
    Piece3D ..> TetrisVector : «create»
    PieceGenerator ..> Piece2D : «create»
    PieceGenerator ..> Piece3D : «create»
    PieceGenerator ..> TetrisVector : «create»
    Ai --> PieceMoveScore
    PieceMoveScore "1" *--> "piece 1" Piece
    RenderUtils ..> ElementBuffer : «create»
    RenderUtils ..> VertexArray : «create»
    RenderUtils ..> VertexBuffer : «create»
    Renderer2D ..> Renderer
    Renderer2D "1" *--> "gridPanels *" TetrisCubePanel
    Renderer2D ..> TetrisCubePanel : «create»
    Renderer2D ..> TetrisVector : «create»
    Renderer2D "1" *--> "vueController 1" VueController
    Renderer3D "1" *--> "camController 1" CameraController
    Renderer3D ..> CameraController : «create»
    Renderer3D ..> Cube : «create»
    Renderer3D ..> OpenGLRenderer : «create»
    Renderer3D "1" *--> "renderer 1" OpenGLRenderer
    Renderer3D ..> Renderer
    Renderer3D ..> TetrisVector : «create»
    Renderer3D "1" *--> "vueController 1" VueController
    SimpleShader --> ShaderProgram
    TetrisCubePanel "1" *--> "borders *" PieceColor
    TextRenderer ..> TextChar : «create»
    TextRenderer ..> TextShader : «create»
    TextRenderer "1" *--> "shader 1" TextShader
    TextRenderer "1" *--> "textVAO 1" VertexArray
    TextRenderer ..> VertexArray : «create»
    TextRenderer ..> VertexBuffer : «create»
    TextShader --> ShaderProgram
    VertexArray "1" *--> "elementBuffer 1" ElementBuffer
    VertexArray "1" *--> "vertexBuffers *" VertexBuffer
    VertexBuffer "1" *--> "vertexAttribs *" VertexAttribPointer
    VertexBuffer ..> VertexAttribPointer : «create»
    VueController ..> MenuRenderer : «create»
    VueController "1" *--> "model 1" Model
    VueController "1" *--> "currentRenderer 1" Renderer
    VueController ..> Renderer2D : «create»
    VueController ..> Renderer3D : «create»
```

### Package `model`

- **Classes Principales :**
  - **`Model`** : Gère la logique du jeu, y compris la rotation des pièces, le démarrage et l'arrêt du jeu, et l'exécution de l'IA.
  - **`Game`** : Représente une instance du jeu, gérant le score, la grille, et les pièces actuelles et suivantes.
  - **`Grid`**, **`Grid2D`**, **`Grid3D`** : Gèrent la grille de jeu, avec des fonctionnalités pour vérifier les collisions, geler les pièces, et effacer les lignes complètes.
  - **`Piece`**, **`Piece2D`**, **`Piece3D`** : Représentent les pièces du jeu, avec des fonctionnalités pour la rotation et le déplacement.
  - **`Ai`** : Implémente l'intelligence artificielle pour jouer au jeu, calculant les meilleurs mouvements possibles.
  - **`GeneticTrainer`** : Utilisé pour entraîner l'IA en utilisant un algorithme génétique.

### Package `vc` (Vue-Contrôleur)

- **Classes Principales :**
  - **`VueController`** : Gère la vue et le contrôleur, coordonnant les interactions entre le modèle et la vue.
  - **`Renderer`**, **`Renderer2D`**, **`Renderer3D`** : Gèrent le rendu du jeu en 2D et 3D.
  - **`OpenGLRenderer`** : Utilise OpenGL pour le rendu graphique.
  - **`CameraController`** : Gère les caméras pour le rendu 3D, permettant de basculer entre une caméra libre et une caméra dirigée.
  - **`TextRenderer`** : Gère le rendu du texte à l'écran.

## Instructions de Build

### Utilisation de Nix (recommandé)

Pour garantir que vous avez les bonnes versions des logiciels et des dépendances, il est recommandé d'utiliser Nix.

```bash
nix build
./result/bin/PIE_Swin
```

Ou simplement :

```bash
nix run
```

### Utilisation de Gradle

Vous pouvez également utiliser Gradle seule pour construire et exécuter le projet.

```bash
gradle fatJar
java -jar build/libs/PIE_Swing-1.0-SNAPSHOT-all.jar
```

Ou simplement :

```bash
gradle run
```
