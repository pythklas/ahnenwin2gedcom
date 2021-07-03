package helperfunctions

import de.ng.ahnenwin2gedcom.helperfunctions.ListFun
import org.junit.Test
import java.util.function.Predicate
import kotlin.test.assertEquals

class ListFunTest {

    @Test
    fun splitListHappyPath() {
        val list = listOf("1", "2", "3")
        val splitCondition = Predicate<String> { it == "2" }
        val splitList = ListFun.splitWhereTrue(list, splitCondition)
        assertEquals(2, splitList.size)
        assertEquals(splitList[0], listOf("1"))
        assertEquals(splitList[1], listOf("3"))
    }
}
