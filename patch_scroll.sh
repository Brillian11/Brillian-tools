sed -i '85a\
    var isFabExpanded by remember { mutableStateOf(true) }\
    val nestedScrollConnection = remember {\
        object : NestedScrollConnection {\
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {\
                if (available.y < -5f) {\
                    isFabExpanded = false\
                } else if (available.y > 5f) {\
                    isFabExpanded = true\
                }\
                return Offset.Zero\
            }\
        }\
    }\
' app/src/main/java/com/example/ui/screens/dashboard/MainTabScreen.kt
