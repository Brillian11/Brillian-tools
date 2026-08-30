sed -i '33a\
import androidx.compose.ui.input.nestedscroll.nestedScroll\
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection\
import androidx.compose.ui.input.nestedscroll.NestedScrollSource\
import androidx.compose.ui.geometry.Offset\
import androidx.compose.animation.core.animateDpAsState\
import androidx.compose.animation.core.tween\
import androidx.compose.foundation.layout.size\
import androidx.compose.animation.Crossfade\
' app/src/main/java/com/example/ui/screens/dashboard/MainTabScreen.kt
