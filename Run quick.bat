javac src/code/core/*.java src/code/world/*.java src/code/world/fixed/*.java src/code/world/fixed/dividers/*.java src/code/world/inv/*.java src/code/world/unit/*.java -cp lib/*/*.jar -d bin

cd bin

start "" java code/core/Core

pause