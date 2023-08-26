import { basicSetup} from "codemirror";

import {EditorView, keymap} from "@codemirror/view";
import {indentWithTab} from "@codemirror/commands";

import {LRLanguage, LanguageSupport} from "@codemirror/language";


import {javascript} from "@codemirror/lang-javascript"

import {parser} from './parser.js';

import {highlighting} from './highlight.js';

const initialDoc = `


`;



const parserWithMetadata = parser.configure({
    props: [highlighting]
});

const minervaLanguage = LRLanguage.define({parser: parserWithMetadata});


const editor = new EditorView({
    doc: initialDoc,
    extensions: [basicSetup,keymap.of(indentWithTab), new LanguageSupport(minervaLanguage)],
    parent: document.getElementById("codemirror")
});


export function getEditorState(){
   if (editor.state.text) return editor.state.text;
   return editor.state.doc.children.reduce((prev, curr) => [...prev, ...curr], []);
};