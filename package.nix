{
  lib,
  stdenv,
  jdk23,
  gradle,
  makeWrapper,
  libGL,
}: let
  jdk = jdk23;
  self = stdenv.mkDerivation (finalAttrs: {
    pname = "PIE_Swin";
    version = "0.0.0";

    src = lib.cleanSource ./.;

    nativeBuildInputs = [
      (gradle.override {java = jdk;})
      jdk
      makeWrapper
    ];

    buildInputs = [
      libGL
    ];

    mitmCache = gradle.fetchDeps {
      pkg = self;
      /*
      To update this file, run:
      nix build .#PIE_Swin.mitmCache.updateScript
      ./result
      */
      data = ./deps.json;
    };

    gradleFlags = ["-Dfile.encoding=utf-8"];

    doCheck = true;

    gradleBuildTask = "fatJar";

    installPhase = ''
      export LD_LIBRARY_PATH=${lib.makeLibraryPath [libGL]}
      mkdir -p $out/{bin,share/PIE_Swin}
      cp build/libs/PIE_Swing-1.0-SNAPSHOT-all.jar $out/share/PIE_Swin

      makeWrapper ${jdk}/bin/java $out/bin/PIE_Swin \
        --prefix LD_LIBRARY_PATH : ${lib.makeLibraryPath [libGL]} \
        --add-flags "-jar $out/share/PIE_Swin/PIE_Swing-1.0-SNAPSHOT-all.jar"
    '';

    meta.sourceProvenance = with lib.sourceTypes; [
      fromSource
      binaryBytecode # mitm cache
    ];
  });
in
  self
