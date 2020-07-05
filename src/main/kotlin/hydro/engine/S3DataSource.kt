package hydro.engine

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import java.io.InputStream

class S3DataSource(
    private val s3Client: AmazonS3,
    private val bucket: String,
    private val key: String
): HydroDataSource {

    override fun load(): InputStream {
        return s3Client.getObject(GetObjectRequest(bucket, key)).objectContent
    }

}