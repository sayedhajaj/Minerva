// This file was generated by lezer-generator. You probably shouldn't edit it.
import {LRParser} from "@lezer/lr"
import {highlighting} from "./highlight.js"
const spec_Identifier = {__proto__:null,class:8, interface:14, module:18, enum:22, function:26, this:28, var:60, const:62, for:66, if:70, else:72, while:76, until:80, print:84, match:88}
export const parser = LRParser.deserialize({
  version: 14,
  states: "1WQQOPOOOOOO'#Cl'#ClOOOO'#Cm'#CmO!bOPO'#CkO#kOPO'#D[O!bOPO'#CsOOOO'#Ct'#CtOOOO'#Dk'#DkOOOO'#Db'#DbOOOO'#D]'#D]QQOPOOO#rOPO'#C^O#wOPO'#CbO#|OPO'#CdO$ROPO'#CfO$WOPO'#ChO$]OQO'#CyO$bOPO'#C|O$gOPO'#DOO$gOPO'#DRO$gOPO'#DTO!bOPO'#DVO$lOPO'#DXO$qOPO,59VOOOO'#Co'#CoOOOO'#Cp'#CpOOOO'#Cq'#CqO&ZOQO'#CrO!bOPO,59YO!bOPO,59YO!bOPO,59YO&uOPO'#CxOOOO,59c,59cOOOO,59v,59vO'POPO,59_OOOO-E7Z-E7ZO'WOPO,58xO']OPO,58|O'bOPO,59OO'gOPO,59QO'WOPO,59SO!bOPO,59eO'lOPO,59hOQOPO,59jOQOPO,59mOQOPO,59oO'yOPO,59qO(QOPO,59sOOOO,59^,59^O*VOPO1G.tO*dOPO1G.tO+VOPO1G.tO,{OPO'#DwO!bOPO'#DwO-VOPO,59dOOOO1G.y1G.yO-[OPO'#CaO-gOPO1G.dO-lOPO1G.hO-qOPO1G.jO-vOPO1G.lO.ROPO1G.nO.WOPO1G/PO/jOPO1G/SO/qOPO1G/SO/xOPO1G/SO0POPO1G/UOOOO1G/X1G/XOOOO1G/Z1G/ZOOOO1G/]1G/]O1gOPO'#DZOOOO'#D`'#D`O1lOPO1G/_O1wOPO'#D_O2OOPO,5:cO2WOPO,5:cOOOO1G/O1G/OO2bOPO'#DdO2mOPO'#DdO2rOPO,58{O2wOPO7+$OOOOO7+$S7+$SOOOO7+$U7+$UO2|OPO7+$WO!bOPO7+$YO3ROPO7+$nO3]OPO7+$nOOOO7+$n7+$nO3dOPO7+$nO3kOPO7+$nOQOPO7+$pO!bOPO,59uOOOO-E7^-E7^OOOO7+$y7+$yO3rOPO,59yO!bOPO,59yOOOO-E7]-E7]O3|OPO1G/}O4UOPO'#D^O4^OPO,5:OO4^OPO,5:OOOOO1G.g1G.gOOOO<<Gj<<GjOOOO<<Gr<<GrO4iOPO<<GtO5xOPO<<HYOOOO<<HY<<HYO6SOPO<<HYO6ZOPO<<HYOOOO<<H[<<H[O6bOPO1G/aO6iOPO1G/eOOOO,59x,59xO6sOPO,59xOOOO-E7[-E7[O6xOPO1G/jOOOOAN=tAN=tO7TOPOAN=tO7[OPOAN=tOOOO7+${7+${OOOO1G/d1G/dO7cOPOG23`OOOOG23`G23`OOOOLD(zLD(z",
  stateData: "7j~OSZOV[OX]OZ^O]_O^VOiUOjUOn`Oo`OqaOsbOvcOxdOzeO|fO!VTO!`PO!aQO!bQO~O^VOiUOjUO!VTO!`PO!aQO!bQO~O!VoO!aQO!bQO!chO!diO!ejO!fkO!gkO!hkO!ikO~O!lqO~P!yORtO~ORuO~ORvO~ORwO~ORxO~O!jyO~O!VzO~O!VTO~O![!PO~O!l_a!Z_a!Y_aS_aV_aX_aZ_a]_a^_ai_aj_an_ao_aq_as_av_ax_az_a|_a!T_a!`_at_a~P!yO!j!QO^fXifXjfX!VfX!`fX!afX!bfX~O!X!VO!Z!kP~P!bO!Z!XO~P!yO!V!YO~O![![O~O![!]O~O![!^O~On`Oo`O!l!bO~P!bO!l!gO~P!yOiUOjUO~O!VoO!abi!bbi!fbi!gbi!hbi!ibi!lbi!Zbi!YbiSbiVbiXbiZbi]bi^biibijbinbiobiqbisbivbixbizbi|bi!Tbi!`bitbi~O!cbi!dbi!ebi~P(YO!chO!diO!ejO~P(YO!VoO!aQO!bQO!chO!diO!ejO~O!fbi!gbi!hbi!ibi!lbi!Zbi!YbiSbiVbiXbiZbi]bi^biibijbinbiobiqbisbivbixbizbi|bi!Tbi!`bitbi~P*qO!Y!kO!Z!kX~P!yO!Z!nO~OR!oO!X!pO!Z!WP~O![!rO~O!]!sO~O!]!tO~OR!oO!X!pO!]!WP~O!^!vO~OSmiVmiXmiZmi]mi^miimijminmiomiqmismivmixmizmi|mi!Tmi!`mi!lmitmi~P!yO!l!wO~P!yO!Z!yO~P!bO!l!{O~P!bOt!|OSriVriXriZri]ri^riirijrinrioriqrisrivrixrizri|ri!Tri!Vri!`ri!ari!bri~O!^!}O~OiUOjUO!]#PO~O!X#RO~P!bO!Y!kO!Z!ka~O!Y!kO!Z!ka~P!yO!Y#UO!Z!WX!]!WX~OR#WO~O!Z#XO~O!]#YO~O!]#ZO~O!Z#^O!l#_O~P!bO!Z#^O~P!yO!l#_O~P!yO!Z#^O~P!bO!Y!Ra!Z!Ra~P!yO!Y!kO!Z!ki~OR#dO!X#eO~O!Y#UO!Z!Wa!]!Wa~OS[yV[yX[yZ[y][y^[yi[yj[yn[yo[yq[ys[yv[yx[yz[y|[y!T[y!`[yt[y~P!yO!Z#hO!l#iO~P!yO!Z#hO~P!bO!Z#hO~P!yO!l#kO~P!yO!Y!Ri!Z!Ri~P!yOR#lO~O!Y#UO!Z!Wi!]!Wi~O!Z#nO~P!bO!Z#nO~P!yO!Z#oO~P!yO",
  goto: "+W!lPP!mPP!u!mP!mP!mP!mPP!{#i$V!{%Y%Y%Y%p&U&{PP!{'m(UPP!mP!mPP!mP!mP!mP!mP(a!m(e(k(u)PP)VP)gPPPPPP)mPPPPPPPPPPP+T]WOY{|}!|Q!ZtR!_x!VVORTYelmnoyz{|}!V!b!c!k!v!w!{!|!}#R#_#i!VRORTYelmnoyz{|}!V!b!c!k!v!w!{!|!}#R#_#i!URORTYelmnoyz{|}!V!b!c!k!v!w!{!|!}#R#_#iwmSgr!O!T!U!`!a!m!x!z#Q#[#]#`#b#c#j#mylSgr!O!S!T!U!`!a!m!x!z#Q#[#]#`#b#c#j#munSgr!O!U!`!a!m!x!z#Q#[#]#`#b#c#j#m!UVORTYelmnoyz{|}!V!b!c!k!v!w!{!|!}#R#_#iQ{bQ|cR}d!UVORTYelmnoyz{|}!V!b!c!k!v!w!{!|!}#R#_#iT!h!P!j{pSgr!O!R!S!T!U!`!a!m!x!z#Q#[#]#`#b#c#j#m[WOY{|}!|R!czT!i!P!jQYORsYQ#V!oS#f#V#gR#g#WQ!l!US#S!l#TR#T!mQ!j!PR#O!jSXOYQ!d{Q!e|Q!f}R#a!|Q!q!YR!u!^[SOY{|}!|QgRQrTQ!OeQ!RlQ!SmQ!TnQ!UoQ!`yQ!azQ!m!VQ!x!bQ!z!cQ#Q!kQ#[!vQ#]!wQ#`!{Q#b!}Q#c#RQ#j#_R#m#iR!Wo",
  nodeNames: "⚠ File ClassDeclaration Identifier class ParamList InterfaceDeclaration interface ModuleDeclaration module EnumDeclaration enum FunctionDeclaration function this UnaryExpression LogicOp ArithOp BinaryExpression ArithOp ArithOp ArithOp CompareOp ParenthesizedExpression Literal Integer String CallExpression ArgList VariableDeclaration var const ForStatement for IfStatement if else WhileStatement while UntilStatement until PrintStatement print MatchStatement match MatchCase ExpressionStatement",
  maxTerm: 74,
  nodeProps: [
    ["group", -13,2,6,8,10,12,29,32,34,37,39,41,43,46,"Statement",-6,14,15,18,23,24,27,"Expression"]
  ],
  propSources: [highlighting],
  skippedNodes: [0],
  repeatNodeCount: 4,
  tokenData: "&R~Reqr!drs!quv#`xy#eyz#jz{#o{|#t|}#y}!O$O!O!P$T!P!Q$f!Q![$k!]!^$s!^!_$x!_!`$}!`!a%d!c!}%i#R#S%i#T#o%i#o#p%w#q#r%|~!iP!`~!_!`!l~!qO!i~~!tTOr!qrs#Ts;'S!q;'S;=`#Y<%lO!q~#YOj~~#]P;=`<%l!q~#eO!d~~#jO!V~~#oO!Z~~#tO!e~~#yO!a~~$OO!Y~~$TO!b~~$WP!O!P$Z~$^P!O!P$a~$fO!X~~$kO!c~~$pPi~!Q![$k~$xO!l~~$}O!f~R%SQ!jQ!_!`%Y!`!a%_P%_O!hPP%dO!^P~%iO!g~~%nRR~!c!}%i#R#S%i#T#o%i~%|O![~~&RO!]~",
  tokenizers: [0, 1],
  topRules: {"File":[0,1]},
  specialized: [{term: 3, get: value => spec_Identifier[value] || -1}],
  tokenPrec: 0
})