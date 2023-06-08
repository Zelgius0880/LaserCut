package com.zelgius.laserCut.display

import com.pi4j.io.i2c.I2C
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.image.Raster
import kotlin.experimental.or


class Display(
    /**
     * @return Display width
     */
    val width: Int,
    /**
     * @return Display height
     */
    val height: Int, private val i2c: I2C
) {

    companion object {
        const val SSD1306_I2C_ADDRESS: Short = 0x3C
        const val SSD1306_SETCONTRAST: Short = 0x81
        const val SSD1306_DISPLAYALLON_RESUME: Short = 0xA4
        const val SSD1306_DISPLAYALLON: Short = 0xA5
        const val SSD1306_NORMALDISPLAY: Short = 0xA6
        const val SSD1306_INVERTDISPLAY: Short = 0xA7
        const val SSD1306_DISPLAYOFF: Short = 0xAE
        const val SSD1306_DISPLAYON: Short = 0xAF
        const val SSD1306_SETDISPLAYOFFSET: Short = 0xD3
        const val SSD1306_SETCOMPINS: Short = 0xDA
        const val SSD1306_SETVCOMDETECT: Short = 0xDB
        const val SSD1306_SETDISPLAYCLOCKDIV: Short = 0xD5
        const val SSD1306_SETPRECHARGE: Short = 0xD9
        const val SSD1306_SETMULTIPLEX: Short = 0xA8
        const val SSD1306_SETLOWCOLUMN: Short = 0x00
        const val SSD1306_SETHIGHCOLUMN: Short = 0x10
        const val SSD1306_SETSTARTLINE: Short = 0x40
        const val SSD1306_MEMORYMODE: Short = 0x20
        const val SSD1306_COLUMNADDR: Short = 0x21
        const val SSD1306_PAGEADDR: Short = 0x22
        const val SSD1306_COMSCANINC: Short = 0xC0
        const val SSD1306_COMSCANDEC: Short = 0xC8
        const val SSD1306_SEGREMAP: Short = 0xA0
        const val SSD1306_CHARGEPUMP: Short = 0x8D
        const val SSD1306_EXTERNALVCC: Short = 0x1
        const val SSD1306_SWITCHCAPVCC: Short = 0x2

        const val SSD1306_ACTIVATE_SCROLL: Short = 0x2F
        const val SSD1306_DEACTIVATE_SCROLL: Short = 0x2E
        const val SSD1306_SET_VERTICAL_SCROLL_AREA: Short = 0xA3
        const val SSD1306_RIGHT_HORIZONTAL_SCROLL: Short = 0x26
        const val SSD1306_LEFT_HORIZONTAL_SCROLL: Short = 0x27
        const val SSD1306_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL: Short = 0x29
        const val SSD1306_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL: Short = 0x2A
    }

    var vccState = 0
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Returns internal AWT image
     * @return BufferedImage
     */
    var image: BufferedImage
        protected set

    /**
     * Returns Graphics object which is associated to current AWT image,
     * if it wasn't set using setImage() with false createGraphics parameter
     * @return Graphics2D object
     */
    var graphics: Graphics2D
        private set

    private val pages: Int = (height + 7) / 8
    private var buffer: ByteArray


    init {
        buffer = ByteArray(width * pages)
        image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY)
        graphics = image.createGraphics()
    }

    private fun initDisplay() {
        if (width == 128 && height == 64) {
            init(0x3F, 0x12, 0x80)
        } else if (width == 128 && height == 32) {
            init(0x1F, 0x02, 0x80)
        } else if (width == 96 && height == 16) {
            init(0x0F, 0x02, 0x60)
        }
    }

    private fun init(multiplex: Int, compins: Int, ratio: Int) {
        command(SSD1306_DISPLAYOFF)
        command(SSD1306_SETDISPLAYCLOCKDIV)
        command(ratio.toShort())
        command(SSD1306_SETMULTIPLEX)
        command(multiplex.toShort())
        command(SSD1306_SETDISPLAYOFFSET)
        command(0x0.toShort())
        command(SSD1306_SETSTARTLINE)
        command(SSD1306_CHARGEPUMP)
        if (vccState.toShort() == SSD1306_EXTERNALVCC) command(0x10.toShort()) else command(
            0x14.toShort()
        )
        command(SSD1306_MEMORYMODE)
        command(0x00.toShort())
        command(((SSD1306_SEGREMAP or 0x1)))
        command(SSD1306_COMSCANDEC)
        command(SSD1306_SETCOMPINS)
        command(compins.toShort())
        command(SSD1306_SETCONTRAST)
        if (vccState.toShort() == SSD1306_EXTERNALVCC) command(0x9F.toShort()) else command(
            0xCF.toShort()
        )
        command(SSD1306_SETPRECHARGE)
        if (vccState.toShort() == SSD1306_EXTERNALVCC) command(0x22.toShort()) else command(
            0xF1.toShort()
        )
        command(SSD1306_SETVCOMDETECT)
        command(0x40.toShort())
        command(SSD1306_DISPLAYALLON_RESUME)
        command(SSD1306_NORMALDISPLAY)
    }

    /**
     * Turns on command mode and sends command
     * @param command Command to send. Should be in short range.
     */
    fun command(command: Short) {
        i2cWrite(0, command.toInt())
    }

    /**
     * Turns on data mode and sends data
     * @param data Data to send. Should be in short range.
     */
    fun data(data: Int) {
        i2cWrite(0x40, data)
    }

    /**
     * Turns on data mode and sends data array
     * @param data Data array
     */
    fun data(data: ByteArray) {
        var i = 0
        while (i < data.size) {
            i2cWrite(0x40, data[i].toInt())
            i++
        }
    }
    /**
     * Begin with specified VCC mode (can be SWITCHCAPVCC or EXTERNALVCC)
     * @param vccState VCC mode
     * @see SSD1306_SWITCHCAPVCC
     *
     * @see SSD1306_EXTERNALVCC
     */
    /**
     * Begin with SWITCHCAPVCC VCC mode
     * @see SSD1306_SWITCHCAPVCC
     */
    @JvmOverloads
    fun begin(vccState: Int = SSD1306_SWITCHCAPVCC.toInt()) {
        this.vccState = vccState
        initDisplay()
        command(SSD1306_DISPLAYON)
        this.clear()
        display()
    }

    /**
     * Sends the buffer to the display
     */
    @Synchronized
    fun display() {
        command(SSD1306_COLUMNADDR)
        command(0)
        command((width - 1).toShort())
        command(SSD1306_PAGEADDR)
        command(0)
        command((pages - 1).toShort())
        data(buffer)
    }

    /**
     * Clears the buffer by creating a new byte array
     */
    fun clear() {
        buffer = ByteArray(width * pages)
    }

    /**
     * Sets the display contract. Apparently not really working.
     * @param contrast Contrast
     */
    fun setContrast(contrast: Byte) {
        command(SSD1306_SETCONTRAST)
        command(contrast.toShort())
    }

    /**
     * Sets if the backlight should be dimmed
     * @param dim Dim state
     */
    fun dim(dim: Boolean) {
        if (dim) {
            setContrast(0.toByte())
        } else {
            if (vccState.toShort() == SSD1306_EXTERNALVCC) {
                setContrast(0x9F.toByte())
            } else {
                setContrast(0xCF.toByte())
            }
        }
    }

    /**
     * Sets if the display should be inverted
     * @param invert Invert state
     */
    fun invertDisplay(invert: Boolean) {
        if (invert) {
            command(SSD1306_INVERTDISPLAY)
        } else {
            command(SSD1306_NORMALDISPLAY)
        }
    }

    /**
     * Probably broken
     */
    fun scrollHorizontally(left: Boolean, start: Int, end: Int) {
        command(if (left) SSD1306_LEFT_HORIZONTAL_SCROLL else SSD1306_RIGHT_HORIZONTAL_SCROLL)
        command(0)
        command(start.toShort())
        command(0)
        command(end.toShort())
        command(1)
        command(0xFF)
        command(SSD1306_ACTIVATE_SCROLL)
    }

    /**
     * Probably broken
     */
    fun scrollDiagonally(left: Boolean, start: Int, end: Int) {
        command(SSD1306_SET_VERTICAL_SCROLL_AREA)
        command(0)
        command(height.toShort())
        command(if (left) SSD1306_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL else SSD1306_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL)
        command(0)
        command(start.toShort())
        command(0)
        command(end.toShort())
        command(1)
        command(SSD1306_ACTIVATE_SCROLL)
    }

    /**
     * Stops scrolling
     */
    fun stopScroll() {
        command(SSD1306_DEACTIVATE_SCROLL)
    }

    /**
     * Sets one pixel in the current buffer
     * @param x X position
     * @param y Y position
     * @param white White or black pixel
     * @return True if the pixel was successfully set
     */
    fun setPixel(x: Int, y: Int, white: Boolean): Boolean {
        if (x < 0 || x > width || y < 0 || y > height) {
            return false
        }
        if (white) {
            buffer[x + y / 8 * width] = (buffer[x + y / 8 * width].toInt() or (1 shl (y and 7))).toByte()
        } else {
            buffer[x + y / 8 * width] = (buffer[x + y / 8 * width].toInt() and (1 shl (y and 7)).inv()).toByte()
        }
        return true
    }

    /**
     * Copies AWT image contents to buffer. Calls display()
     * @see Display.display
     */
    @Synchronized
    fun displayImage() {
        val r: Raster = image.raster
        for (y in 0 until height) {
            for (x in 0 until width) {
                setPixel(x, y, r.getSample(x, y, 0) > 0)
            }
        }
        display()
    }

    /**
     * Sets internal buffer
     * @param buffer New used buffer
     */
    fun setBuffer(buffer: ByteArray) {
        this.buffer = buffer
    }

    /**
     * Sets one byte in the buffer
     * @param position Position to set
     * @param value Value to set
     */
    fun setBufferByte(position: Int, value: Byte) {
        buffer[position] = value
    }

    /**
     * Sets internal AWT image to specified one.
     * @param img BufferedImage to set
     * @param createGraphics If true, createGraphics() will be called on the image and the result will be saved
     * to the internal Graphics field accessible by getGraphics() method
     */
    fun setImage(img: BufferedImage, createGraphics: Boolean) {
        image = img
        if (createGraphics) {
            graphics = img.createGraphics()
        }
    }

    private fun i2cWrite(register: Int, value: Int) {
        i2c.writeRegister(register, (value and 0xFF).toByte())
    }
}