package de.ng.ahnenwin2gedcom.helperfunctions

import java.util.*
import java.util.function.Predicate

object ListFun {
    /**
     * Splits the given list where the splitCondition is true. The elements where the condition was true,
     * will not be added to the split result.
     * @param list The given list
     * @param splitCondition The condition that returns true iff it should be a splitting point.
     * @return The split list as a list of lists. The used list implementation for the outer and inner lists is [LinkedList].
     */
    @JvmStatic
    fun <T> splitWhereTrue(list: List<T>, splitCondition: Predicate<T>): List<List<T>> {
        val result: MutableList<MutableList<T>> = LinkedList()
        result.add(0, LinkedList<T>())
        for (t in list) {
            if (splitCondition.test(t)) {
                result.add(0, LinkedList<T>())
                continue
            }
            result[0].add(t)
        }
        result.reverse()
        return result
    }
}
