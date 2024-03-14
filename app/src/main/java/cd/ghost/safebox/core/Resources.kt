package cd.ghost.safebox.core

interface Resources {
    operator fun invoke(id: Int): String
}