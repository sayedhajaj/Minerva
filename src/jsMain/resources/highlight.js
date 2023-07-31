import {styleTags, tags} from "@lezer/highlight"

export const highlighting = styleTags({
  Identifier: tags.name,
  Integer: tags.literal,
  String: tags.comment,
  "if while else until match typematch for": tags.controlKeyword,
  "var const function class interface module enum": tags.definitionKeyword,
  "print": tags.keyword
})