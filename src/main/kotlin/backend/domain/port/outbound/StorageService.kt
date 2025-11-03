package backend.domain.port.outbound

/**
 * Puerto de salida para cualquier servicio de almacenamiento de archivos (Cloudinary, S3, etc.)
 */
interface StorageService {
    /**
     * Sube un archivo.
     * @param fileBytes Los bytes del archivo.
     * @param originalFileName El nombre original del archivo (para usar como pista).
     * @return La URL segura (https) del archivo subido.
     */
    suspend fun uploadFile(fileBytes: ByteArray, originalFileName: String): String
}