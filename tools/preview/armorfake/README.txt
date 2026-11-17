Preview stand-ins for the few Minecraft classes the owner's armor model (input/OnePiece_Armor_1.20.1.zip, OnePieceArmorModel.java, Mojang
names) builds itself with, so tools/preview/ArmorPreview.java can bake exactly the pack's geometry off-game. They do what vanilla's do with
the same calls (a cube's corners and its UVs are laid out in ArmorPreview as vanilla's ModelPart.Cube lays them). Never packaged: build.py
compiles only src/main/java (against stubs/).
