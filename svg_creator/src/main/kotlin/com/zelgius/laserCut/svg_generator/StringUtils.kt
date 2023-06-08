package com.zelgius.laserCut.svg_generator

import java.awt.FontMetrics


object StringUtils {
    /**
     * Returns an array of strings, one for each line in the string after it has
     * been wrapped to fit lines of <var>maxWidth</var>. Lines end with any of
     * cr, lf, or cr lf. A line ending at the end of the string will not output a
     * further, empty string.
     *
     *
     * This code assumes <var>str</var> is not `null`.
     *
     * @param str
     * the string to split
     * @param fm
     * needed for string width calculations
     * @param maxWidth
     * the max line width, in points
     * @return a non-empty list of strings
     */
    fun String.wrap(fm: FontMetrics, maxWidth: Int): List<String> {
        val lines = splitIntoLines(this)
        if (lines.isEmpty()) return lines
        val strings = mutableListOf<String>()
        val iter = lines.iterator()
        while (iter.hasNext()) {
            wrapLineInto(iter.next(), strings, fm, maxWidth)
        }
        return strings
    }

    /**
     * Given a line of text and font metrics information, wrap the line and add
     * the new line(s) to <var>list</var>.
     *
     * @param line
     * a line of text
     * @param list
     * an output list of strings
     * @param fm
     * font metrics
     * @param maxWidth
     * maximum width of the line(s)
     */
    private fun wrapLineInto(l: String, list: MutableList<String>, fm: FontMetrics, maxWidth: Int) {
        var line = l
        var len = line.length
        var width = 0
        while (len > 0 && fm.stringWidth(line).also { width = it } > maxWidth) {
            // Guess where to split the line. Look for the next space before
            // or after the guess.
            val guess = /*len **/ maxWidth / width
            var before = line.substring(0, guess).trim()
            width = fm.stringWidth(before)
            var pos: Int
            if (width > maxWidth) // Too long
                pos = findBreakBefore(line, guess)
            else { // Too short or possibly just right
                pos = findBreakAfter(line, guess)
                if (pos != -1) { // Make sure this doesn't make us too long
                    before = line.substring(0, pos).trim()
                    if (fm.stringWidth(before) > maxWidth) pos = findBreakBefore(line, guess)
                }
            }
            if (pos == -1) pos = guess // Split in the middle of the word
            list.add(line.substring(0, pos).trim())
            line = line.substring(pos).trim()
            len = line.length
        }
        if (len > 0) list.add(line)
    }

    /**
     * Returns the index of the first whitespace character or '-' in <var>line</var>
     * that is at or before <var>start</var>. Returns -1 if no such character is
     * found.
     *
     * @param line
     * a string
     * @param start
     * where to star looking
     */
    private fun findBreakBefore(line: String, start: Int): Int {
        for (i in start downTo 0) {
            val c = line[i]
            if (Character.isWhitespace(c) || c == '-') return i
        }
        return -1
    }

    /**
     * Returns the index of the first whitespace character or '-' in <var>line</var>
     * that is at or after <var>start</var>. Returns -1 if no such character is
     * found.
     *
     * @param line
     * a string
     * @param start
     * where to star looking
     */
    private fun findBreakAfter(line: String, start: Int): Int {
        val len = line.length
        for (i in start until len) {
            val c = line[i]
            if (Character.isWhitespace(c) || c == '-') return i
        }
        return -1
    }

    /**
     * Returns an array of strings, one for each line in the string. Lines end
     * with any of cr, lf, or cr lf. A line ending at the end of the string will
     * not output a further, empty string.
     *
     *
     * This code assumes <var>str</var> is not `null`.
     *
     * @param str
     * the string to split
     * @return a non-empty list of strings
     */
    private fun splitIntoLines(str: String): List<String> {
        val strings = mutableListOf<String>()
        val len = str.length
        if (len == 0) {
            strings.add("")
            return strings
        }
        var lineStart = 0
        var i = 0
        while (i < len) {
            val c = str[i]
            if (c == '\r') {
                var newlineLength = 1
                if (i + 1 < len && str[i + 1] == '\n') newlineLength = 2
                strings.add(str.substring(lineStart, i))
                lineStart = i + newlineLength
                if (newlineLength == 2) // skip \n next time through loop
                    ++i
            } else if (c == '\n') {
                strings.add(str.substring(lineStart, i))
                lineStart = i + 1
            }
            ++i
        }
        if (lineStart < len) strings.add(str.substring(lineStart))
        return strings
    }
}