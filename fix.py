with open("app/src/main/java/com/example/ui/screens/dashboard/MainTabScreen.kt", "r") as f:
    content = f.read()

content = content.replace("        showCameraHubScreen = false\n    var isFabExpanded by remember { mutableStateOf(true) }", "        showCameraHubScreen = false\n    }\n\n    var isFabExpanded by remember { mutableStateOf(true) }")
content = content.replace("        }\n    }\n    }\n    // Instantiate", "        }\n    }\n\n    // Instantiate")

with open("app/src/main/java/com/example/ui/screens/dashboard/MainTabScreen.kt", "w") as f:
    f.write(content)
