package cd.ghost.safebox.domain.entities

import cd.ghost.safebox.R

sealed class FileType(
    val icon: Int,
    val type: String
) {
    object AVI : FileType(icon = R.drawable.type_avi, type = "avi")
    object BMP : FileType(icon = R.drawable.type_bmp, type = "bmp")
    object CRD : FileType(icon = R.drawable.type_crd, type = "crd")
    object DOC : FileType(icon = R.drawable.type_doc, type = "doc")
    object DOCX : FileType(icon = R.drawable.type_docx, type = "docx")
    object FLV : FileType(icon = R.drawable.type_flv, type = "flv")
    object GIFF : FileType(icon = R.drawable.type_giff, type = "giff")
    object JPG : FileType(icon = R.drawable.type_jpg, type = "jpg")
    object MOV : FileType(icon = R.drawable.type_mov, type = "mov")
    object MP3 : FileType(icon = R.drawable.type_mp3, type = "mp3")
    object MP4 : FileType(icon = R.drawable.type_mp4, type = "mp4")
    object MPEG : FileType(icon = R.drawable.type_mpeg, type = "mpeg")
    object PDF : FileType(icon = R.drawable.type_pdf, type = "pdf")
    object PNG : FileType(icon = R.drawable.type_png, type = "png")
    object PPT : FileType(icon = R.drawable.type_ppt, type = "ppt")
    object PUB : FileType(icon = R.drawable.type_pub, type = "pub")
    object RAR : FileType(icon = R.drawable.type_rar, type = "rar")
    object RAW : FileType(icon = R.drawable.type_raw, type = "raw")
    object TIFF : FileType(icon = R.drawable.type_tiff, type = "tiff")
    object TXT : FileType(icon = R.drawable.type_txt, type = "txt")
    object XSL : FileType(icon = R.drawable.type_xsl, type = "xsl")
    object ZIP : FileType(icon = R.drawable.type_zip, type = "zip")
    object UNKNOWN : FileType(icon = R.drawable.type_unknown, type = "")

    companion object {
        val typeList = listOf(
            AVI,
            BMP,
            CRD,
            DOC,
            DOCX,
            FLV,
            GIFF,
            JPG,
            MOV,
            MOV,
            MP3,
            MP4,
            MPEG,
            PDF,
            PNG,
            PPT,
            PUB,
            RAR,
            RAW,
            TIFF,
            TXT,
            XSL,
            ZIP
        )

        /**
         * The [ednOfName] comes without dot that's why we need to add dot at the start of the string
         */
        fun findType(type: String): FileType =
            typeList.find { it.type == type } ?: UNKNOWN

    }
}

