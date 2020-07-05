package hydro.engine

import java.io.InputStream

interface HydroDataSource {

    fun load(): InputStream

}