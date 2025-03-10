package srangeldev.storage

import nl.adaptivity.xmlutil.serialization.XML
import org.lighthousegames.logging.logging
import srangeldev.dto.PersonalCsvDto
import srangeldev.dto.EquipoDto
import srangeldev.exceptions.PersonalException
import srangeldev.mapper.toCsvDto
import srangeldev.mapper.toEntrenador
import srangeldev.mapper.toJugador
import srangeldev.models.Entrenador
import srangeldev.models.Jugador
import srangeldev.models.Personal
import java.io.File

/**
 * Clase que implementa el almacenamiento de personal en formato XML.
 */
class PersonalStorageXml : PersonalStorageFile {
    private val logger = logging()

    /**
     * Inicializa el almacenamiento de personal en formato XML.
     */
    init {
        logger.debug { "Inicializando almacenamiento de personal en formato XML" }
    }

    /**
     * Lee los datos de personal desde un archivo XML.
     *
     * @param file El archivo XML desde el cual leer.
     * @return Una lista de datos de personal.
     * @throws PersonalException.PersonalStorageException Si el archivo no existe, no es legible, o no es un archivo XML válido.
     */
    override fun readFromFile(file: File): List<Personal> {
        if (!file.exists() || !file.isFile || !file.canRead() || file.length() == 0L || !file.name.endsWith(".xml", true)) {
            logger.error { "El fichero no existe o es un fichero que no se puede leer: $file" }
            throw PersonalException.PersonalStorageException("El fichero no existe o es un fichero que no se puede leer: $file")
        }
        val xml = XML {}
        val xmlString = file.readText()
        val personalDto: EquipoDto = xml.decodeFromString(EquipoDto.serializer(), xmlString)
        val personalListDto = personalDto.equipo
        return personalListDto.map {
            when (it.rol) {
                "Entrenador" -> it.toEntrenador()
                "Jugador" -> it.toJugador()
                else -> throw IllegalArgumentException("Tipo de Personal desconocido")
            }
        }
    }

    /**
     * Escribe los datos de personal en un archivo XML.
     *
     * @param file El archivo XML en el cual escribir.
     * @param personalList La lista de datos de personal a escribir.
     * @throws PersonalException.PersonalStorageException Si el directorio padre no existe, no es un directorio, o el archivo no tiene una extensión XML.
     */
    override fun writeToFile(file: File, personalList: List<Personal>) {
        logger.debug { "Escribiendo personal en formato de fichero XML: $file" }
        if (!file.parentFile.exists() || !file.parentFile.isDirectory || !file.name.endsWith(".xml", true)) {
            logger.error { "El directorio padre del fichero no existe o no es un directorio o el fichero no tiene extensión XML: ${file.parentFile.absolutePath}" }
            throw PersonalException.PersonalStorageException("El directorio padre del fichero no existe o no es un directorio o el fichero no tiene extensión XML: ${file.parentFile.absolutePath}")
        }
        val xml = XML {}
        val personalListDto: List<PersonalCsvDto> = personalList.map {
            when (it) {
                is Entrenador -> it.toCsvDto()
                is Jugador -> it.toCsvDto()
                else -> throw IllegalArgumentException("Tipo de Personal desconocido")
            }
        }
        val personalDto = EquipoDto(equipo = personalListDto)
        file.writeText(xml.encodeToString(EquipoDto.serializer(), personalDto))
    }
}