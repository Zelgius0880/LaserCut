package com.zelgius.laserCut.svg_generator

import com.zelgius.laserCut.svg_generator.StringUtils.wrap
import org.apache.batik.dom.GenericDOMImplementation
import org.apache.batik.svggen.SVGGeneratorContext
import org.apache.batik.svggen.SVGGraphics2D
import org.apache.commons.cli.*
import org.w3c.dom.DOMImplementation
import org.w3c.dom.Document
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.io.File
import java.io.OutputStreamWriter
import java.io.Writer
import java.text.ParseException
import kotlin.math.roundToInt


const val DEFAULT_TEXT_SIZE = 80
const val DEFAULT_HEIGHT = 300
const val DEFAULT_WIDTH = 800
fun main(args: Array<String>) {

    val cliOptions = setupCLI(if (args.isEmpty()) arrayOf("-t", "$DEFAULT_TEXT_SIZE") else args)

    val out = File(cliOptions.output)
    if (!out.exists()) out.mkdirs()

    drawText(
        File(cliOptions.input).readLines(),
        cliOptions
    )
}


fun drawText(text: List<String>, option: CLIOptions) {
    // Get a DOMImplementation.
    // Get a DOMImplementation.
    val domImpl: DOMImplementation = GenericDOMImplementation.getDOMImplementation()

    println(option)

    // Create an instance of org.w3c.dom.Document.

    // Create an instance of org.w3c.dom.Document.
    val svgNS = "http://www.w3.org/2000/svg"
    val document: Document = domImpl.createDocument(svgNS, "svg", null)

    // Create an instance of the SVG Generator.

    // Create an instance of the SVG Generator.
    val ctx = SVGGeneratorContext.createDefault(document)
    val g = SVGGraphics2D(ctx, false)

    text.forEachIndexed { index, t ->
        g.paint = Color.BLACK
        g.font = Font("Aref Ruqaa Ink", Font.PLAIN, option.textSize)

        val out = File(option.output, "out$index.svg").outputStream()

        val lines = t.wrap(g.fontMetrics, (option.width * 0.9).roundToInt())

        val totalHeight = lines.size * option.textSize
        lines.forEachIndexed { index, s ->
            val i = index + 1
            val bounds = g.fontMetrics.getStringBounds(s, g)
            val width = bounds.width.roundToInt()
            val height = option.textSize//bounds.height.roundToInt()
            g.drawString(
                s,
                (option.width - width - (width * 0.1).roundToInt()) / 2,
                option.height/2 - totalHeight/2 + height*i - height /4
            )
        }

        g.svgCanvasSize = Dimension(option.width, option.height)

        val outWriter: Writer = OutputStreamWriter(out, "UTF-8")
        g.stream(outWriter, true)
    }
}

fun setupCLI(args: Array<String>): CLIOptions {
    val commandLine: CommandLine
    val optionT: Option = Option.builder("t")
        .required(false)
        .hasArg()
        .desc("The text size in pixel")
        .longOpt("textSize")
        .build()

    val optionF: Option = Option.builder("f")
        .required(false)
        .type(Int::class.java)
        .hasArg()
        .desc("The font to use")
        .longOpt("font")
        .build()

    val optionW: Option = Option.builder("w")
        .required(false)
        .type(Int::class.java)
        .hasArg()
        .desc("Canvas width")
        .longOpt("width")
        .build()

    val optionH: Option = Option.builder("h")
        .required(false)
        .desc("Canvas height")
        .hasArg()
        .type(Int::class.java)
        .longOpt("height")
        .build()

    val optionI: Option = Option.builder("i")
        .required(false)
        .desc("Input file")
        .hasArg()
        .type(String::class.java)
        .longOpt("input")
        .build()

    val optionO: Option = Option.builder("o")
        .required(false)
        .type(String::class.java)
        .desc("Output directory")
        .hasArg()
        .longOpt("output")
        .build()

    val options = Options()
    val parser: CommandLineParser = DefaultParser()


    options.addOption(optionT)
    options.addOption(optionF)
    options.addOption(optionH)
    options.addOption(optionI)
    options.addOption(optionO)
    options.addOption(optionH)
    options.addOption(optionW)

    try {
        commandLine = parser.parse(options, args)
        val textSize = if (commandLine.hasOption("t")) {
            commandLine.getOptionValue("t").toInt()
        } else DEFAULT_TEXT_SIZE

        val height = if (commandLine.hasOption("h")) {
            commandLine.getOptionValue("h").toInt()
        } else DEFAULT_HEIGHT

        val width = if (commandLine.hasOption("w")) {
            commandLine.getOptionValue("w").toInt()
        } else DEFAULT_WIDTH

        val font = if (commandLine.hasOption("f")) {
            commandLine.getOptionValue("f")
        } else null

        val input = if (commandLine.hasOption("i")) {
            commandLine.getOptionValue("i")
        } else "in"

        val output = if (commandLine.hasOption("o")) {
            commandLine.getOptionValue("o")
        } else "out"

        return CLIOptions(height, width, font, textSize, input, output)

    } catch (exception: ParseException) {
        exception.printStackTrace()
    }

    return CLIOptions()
}


data class CLIOptions(
    val height: Int = DEFAULT_HEIGHT,
    val width: Int = DEFAULT_WIDTH,
    val font: String? = null,
    val textSize: Int = DEFAULT_TEXT_SIZE,
    val input: String = "out",
    val output: String = "in",
)