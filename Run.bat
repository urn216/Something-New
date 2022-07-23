javac src/code/core/*.java src/code/math/*.java src/code/ui/*.java src/code/ui/elements/*.java src/code/ui/interactables/*.java src/code/world/*.java src/code/world/fixed/*.java src/code/world/fixed/dividers/*.java src/code/world/inv/*.java src/code/world/unit/*.java -d bin

cd bin

jar cfm ../versions/Game.jar data/compiler/manifest.txt code data

start "" java -jar ../versions/Game.jar

pause