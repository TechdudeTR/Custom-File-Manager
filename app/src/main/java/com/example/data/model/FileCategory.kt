package com.example.data.model

enum class FileCategory(val displayName: String, val iconName: String) {
    FOLDER("Folders", "folder"),
    IMAGE("Images", "image"),
    DOCUMENT("Documents", "description"),
    VIDEO("Videos", "movie"),
    AUDIO("Audio", "audiotrack"),
    ARCHIVE("Archives", "archive"),
    APP("Apps", "android"),
    SYSTEM("System Files", "settings_suggest"),
    VAULT("Encrypted Vault", "lock"),
    OTHER("Other", "insert_drive_file");

    companion object {
        fun fromExtension(ext: String, isDir: Boolean, isHiddenOrSys: Boolean, isVault: Boolean): FileCategory {
            if (isVault) return VAULT
            if (isDir) return FOLDER
            if (isHiddenOrSys) return SYSTEM

            return when (ext.lowercase()) {
                "jpg", "jpeg", "png", "gif", "webp", "svg", "bmp" -> IMAGE
                "pdf", "doc", "docx", "txt", "md", "xls", "xlsx", "ppt", "pptx", "csv" -> DOCUMENT
                "mp4", "mkv", "avi", "mov", "webm", "flv" -> VIDEO
                "mp3", "wav", "flac", "aac", "ogg", "m4a" -> AUDIO
                "zip", "tar", "gz", "7z", "rar" -> ARCHIVE
                "apk", "aab" -> APP
                else -> OTHER
            }
        }
    }
}
