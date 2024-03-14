package cd.ghost.safebox.presentation.components

import cd.ghost.safebox.R

class ButtonItem(val icon: Int? = null, val label: String)

val buttons = ArrayList<ButtonItem>().apply {
    for (i in 1..9) {
        add(ButtonItem(label = "$i"))
    }
    add(ButtonItem(label = "<", icon = R.drawable.baseline_backspace_24))
    add(ButtonItem(label = "0"))
    add(ButtonItem(label = "ok", icon = R.drawable.baseline_check_24))
}