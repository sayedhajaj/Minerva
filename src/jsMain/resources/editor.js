import { basicSetup} from "codemirror"

import {EditorView, keymap} from "@codemirror/view"
import {indentWithTab} from "@codemirror/commands"

const initialDoc = `


`

const editor = new EditorView({
    doc: initialDoc,
    extensions: [basicSetup, keymap.of([indentWithTab])],
    parent: document.getElementById("codemirror")
})

export function getEditorState(){
   return editor.state.doc.text
}