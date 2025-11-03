package backend.infrastructure.outbound.storage

import backend.domain.port.outbound.StorageService
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class CloudinaryStorageService(
    private val cloudinary: Cloudinary // Koin inyectará esto
) : StorageService {

    private val MAX_FILE_SIZE = 5 * 1024 * 1024 // 5MB

    override suspend fun uploadFile(fileBytes: ByteArray, originalFileName: String): String {
        validateFile(fileBytes)

        // La subida de archivos es una operación de I/O (bloqueante)
        // Usamos withContext(Dispatchers.IO) para moverla a un hilo de I/O
        return withContext(Dispatchers.IO) {
            try {
                // Opciones de subida
                val options = ObjectUtils.asMap(
                    "resource_type", "auto",
                    "public_id", getPublicIdFromFileName(originalFileName)
                )

                // Sube los bytes
                val result = cloudinary.uploader().upload(fileBytes, options)

                // Devuelve la URL segura
                result["secure_url"]?.toString()
                    ?: throw IOException("No se pudo obtener la URL segura de Cloudinary.")

            } catch (e: Exception) {
                // Aquí deberías loggear el error
                throw IOException("Error al subir el archivo a Cloudinary: ${e.message}", e)
            }
        }
    }

    // Helper para crear un public_id único (ej: "fotos/mi-imagen-123456")
    private fun getPublicIdFromFileName(fileName: String): String {
        val nameWithoutExtension = fileName.substringBeforeLast(".")
        val uniqueId = System.currentTimeMillis().toString().takeLast(6)
        return "agora-profiles/$nameWithoutExtension-$uniqueId"
    }

    private fun validateFile(fileBytes: ByteArray) {
        if (fileBytes.isEmpty()) {
            throw IllegalArgumentException("El archivo no puede estar vacío.")
        }
        if (fileBytes.size > MAX_FILE_SIZE) {
            throw IllegalArgumentException("El archivo excede el tamaño máximo de 5MB.")
        }
    }
}