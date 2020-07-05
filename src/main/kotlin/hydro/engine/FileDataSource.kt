package hydro.engine

import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class FileDataSource(
    private val file: File
): HydroDataSource {

    constructor(filename: String): this(File(filename))

    override fun load(): InputStream {
        return FileInputStream(file)
    }
}