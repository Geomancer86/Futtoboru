# Positions 1888-89: sources for `squads.json` (H-04, BOOK3PRO, 2026-11-28)

Replaces `placeholder` positions in `gamedata/core/seasons/1888-89/squads.json` (DATA_MODEL.md "Positions and
dribbling") with sourced ones. Only placeholders were touched; entries already sourced from `PLAYERS_1888-89.md` were
left as they are, even where a source below disagrees (listed at the end). No id changed.

## Source codes (`positions.<id>.source`)
| Code | Meaning |
|---|---|
| `wp-season-1888-89` | The club's "1888–89 <Club> F.C. season" Wikipedia article names the slot. Its appearance table gives only the line (GK / FB / HB / FW): for goalkeeper, full-back and half-back the line is the slot (backs and halves carry no side). Everton's article names forward slots ("outside left", "inside right", ...; "centre back" read as full-back). |
| `wp-season-1888-89-line` | That article says only FW: the forward slot was assigned (the slot the squad is shortest of; a winger/inside forward named in the player's own article stays on the wing/inside). |
| `wp-player` | The player's own Wikipedia article names the slot: its 1888-89 text where it has one (e.g. "12 at Outside-Right"), else its infobox (career position). Modern infobox words read as: "Midfielder"/"wing half"/"centre half" = half-back, "Defender"/"right back" = full-back. |
| `wp-player-line` | The player's own article says only "Forward" / "Winger" / "Inside forward": the slot was assigned within that line. |
| `fa-cup-final-1889` | Slot from the order of the Wolves line-up in the 1889 FA Cup final (right wing, inside right, centre, inside left, left wing). |

## Counts (placeholder places before -> after)
League clubs: **142 -> 4**. FA Cup clubs: **242 -> 204**. All: **384 -> 208** of 548. After: 117 `players-1888-89`,
47 `players-1888-89-line`, 81 `wp-season-1888-89`, 40 `wp-season-1888-89-line`, 41 `wp-player`, 12 `wp-player-line`,
2 `fa-cup-final-1889`, 208 `placeholder`.

| Club | Squad | Before | After | Generated people (cannot be sourced) |
|---|---|---|---|---|
| preston-north-end | 18 | 5 | 0 | 0 |
| aston-villa | 19 | 8 | 1 | 0 |
| wolverhampton-wanderers | 19 | 9 | 0 | 0 |
| blackburn-rovers | 23 | 11 | 1 | 0 |
| bolton-wanderers | 24 | 13 | 0 | 0 |
| west-bromwich-albion | 20 | 8 | 0 | 0 |
| accrington | 22 | 10 | 0 | 0 |
| everton | 35 | 25 | 0 | 0 |
| burnley | 19 | 2 | 2 | 0 |
| derby-county | 27 | 15 | 0 | 0 |
| notts-county | 35 | 18 | 0 | 0 |
| stoke | 32 | 18 | 0 | 0 |
| old-westminsters | 11 | 10 | 9 | 4 |
| swifts | 11 | 10 | 8 | 3 |
| old-brightonians | 11 | 11 | 9 | 0 |
| wrexham | 11 | 11 | 7 | 0 |
| nottingham-forest | 11 | 10 | 7 | 2 |
| linfield | 11 | 10 | 7 | 2 |
| witton | 11 | 11 | 11 | 11 |
| bootle | 12 | 12 | 7 | 0 |
| chatham-town | 11 | 11 | 11 | 0 |
| south-shore | 11 | 11 | 11 | 0 |
| grimsby-town | 11 | 11 | 11 | 0 |
| sunderland-albion | 11 | 10 | 8 | 1 |
| halliwell | 11 | 11 | 11 | 0 |
| crewe-alexandra | 11 | 11 | 8 | 3 |
| old-carthusians | 11 | 10 | 4 | 0 |
| derby-junction | 11 | 11 | 11 | 9 |
| notts-rangers | 11 | 10 | 9 | 9 |
| sheffield-wednesday | 12 | 10 | 9 | 0 |
| birmingham-st-georges | 11 | 11 | 9 | 9 |
| long-eaton-rangers | 11 | 11 | 11 | 10 |
| small-heath | 11 | 9 | 7 | 1 |
| walsall | 11 | 9 | 8 | 7 |
| heeley | 11 | 11 | 11 | 11 |

Still placeholder in the League: Bob Thomas (Villa) and Thomas Midgley, Robert Duckworth (Burnley) are not in their
season articles; James Duerden (Blackburn) is listed there with position "U" (unknown). FA Cup squads: most are the
legacy rosters flagged in `docs/data/REVIEW_1888-89.md` (copied or invented names: Chatham's Ash family, South Shore's
Birkets, Grimsby's "Chris" x11, Halliwell's Smiths, and 82 `confidence: placeholder` people), so there is nothing to
look up until those rosters are rebuilt from real line-ups. No FA Cup club has a 1888-89 season article.
Wrexham: Bob Roberts is a full back (and once an emergency goalkeeper, [W:Bob_Roberts_(footballer,_born_1863)](https://en.wikipedia.org/wiki/Bob_Roberts_(footballer,_born_1863))),
but with three sourced full-backs in an 11-man squad he stays a placeholder (right wing) so the squad can field a 2-3-5.
The 2-3-5 check also moved 19 remaining FA Cup placeholders to the slot their squad lacked.

## URLs
Season articles (`wp-season-1888-89`, `-line`): `https://en.wikipedia.org/wiki/1888%E2%80%9389_<Club>_F.C._season` for
Preston_North_End, Aston_Villa, Wolverhampton_Wanderers, Blackburn_Rovers, Bolton_Wanderers, West_Bromwich_Albion,
Accrington, Everton, Burnley, Derby_County, Notts_County, Stoke. 1889 final: https://en.wikipedia.org/wiki/1889_FA_Cup_final
(Wykes inside-right, Brodie centre-forward).

Player articles (`wp-player`, `wp-player-line`):

| Club | Person | Slot | Code | Source |
|---|---|---|---|---|
| blackburn-rovers | james-beresford-1860 | right-wing | wp-player | https://en.wikipedia.org/wiki/James_Beresford_(footballer,_born_1860) |
| blackburn-rovers | james-stothert-1870 | inside-left | wp-player | https://en.wikipedia.org/wiki/James_Stothert |
| blackburn-rovers | bernard-whittaker-1865 | inside-left | wp-player | https://en.wikipedia.org/wiki/Bernard_Whittaker |
| bolton-wanderers | thomas-coupar-1865 | centre-forward | wp-player | https://en.wikipedia.org/wiki/Thomas_Coupar |
| west-bromwich-albion | george-haynes-1866 | left-wing | wp-player | https://en.wikipedia.org/wiki/George_Haynes_(footballer) |
| accrington | robert-macbeth-1866 | left-wing | wp-player | https://en.wikipedia.org/wiki/Robert_Macbeth |
| accrington | arthur-wilkinson-1865 | right-wing | wp-player | https://en.wikipedia.org/wiki/Arthur_Wilkinson_(footballer) |
| derby-county | john-lees-1865 | inside-right | wp-player | https://en.wikipedia.org/wiki/John_Lees_(footballer) |
| derby-county | benjamin-spilsbury-1865 | inside-right | wp-player | https://en.wikipedia.org/wiki/Benjamin_Spilsbury |
| notts-county | thomas-allin-1865 | centre-forward | wp-player | https://en.wikipedia.org/wiki/Thomas_Allin_(footballer) |
| stoke | george-slater-1865 | right-wing | wp-player | https://en.wikipedia.org/wiki/George_Slater |
| stoke | bob-barr-1864 | centre-forward | wp-player | https://en.wikipedia.org/wiki/Bob_Barr_(footballer,_born_1865) |
| stoke | freddie-gee-1865 | centre-forward | wp-player | https://en.wikipedia.org/wiki/Freddie_Gee |
| stoke | billy-hutchinson-1865 | right-wing | wp-player | https://en.wikipedia.org/wiki/Billy_Hutchinson_(footballer,_born_1870) |
| stoke | thomas-moore-1865 | centre-forward | wp-player | https://en.wikipedia.org/wiki/Thomas_Moore_(footballer) |
| old-westminsters | rupert-sandilands-1868 | left-wing | wp-player | https://en.wikipedia.org/wiki/Rupert_Sandilands |
| swifts | bambridge-ernest-1848 | half-back | wp-player | https://en.wikipedia.org/wiki/Ernest_Bambridge |
| swifts | francis-pawson-1861 | inside-right | wp-player-line | https://en.wikipedia.org/wiki/Francis_Pawson |
| old-brightonians | nc-cooper-1864 | half-back | wp-player | https://en.wikipedia.org/wiki/Norman_Cooper_(sportsman) |
| old-brightonians | gh-cotterill-1868 | inside-right | wp-player | https://en.wikipedia.org/wiki/George_Cotterill_(footballer) |
| wrexham | george-owen-1865 | inside-right | wp-player-line | https://en.wikipedia.org/wiki/George_Owen_(footballer) |
| wrexham | seth-powell-1862 | full-back | wp-player | https://en.wikipedia.org/wiki/Seth_Powell |
| wrexham | john-price-1854 | centre-forward | wp-player | https://en.wikipedia.org/wiki/John_Price_(footballer,_born_1854) |
| wrexham | jack-powell-1860 | full-back | wp-player | https://en.wikipedia.org/wiki/Jack_Powell_(footballer,_born_1860) |
| nottingham-forest | albert-smith-1869 | half-back | wp-player | https://en.wikipedia.org/wiki/Albert_Smith_(footballer,_born_1869) |
| nottingham-forest | sam-widdowson-1851 | right-wing | wp-player-line | https://en.wikipedia.org/wiki/Sam_Widdowson |
| nottingham-forest | frank-burton-1865 | inside-right | wp-player | https://en.wikipedia.org/wiki/Frank_Burton_(footballer,_born_1865) |
| linfield | george-gaffikin-1868 | inside-right | wp-player | https://en.wikipedia.org/wiki/George_Gaffikin |
| linfield | william-dalton-1864 | right-wing | wp-player-line | https://en.wikipedia.org/wiki/William_Dalton_(footballer) |
| linfield | robert-milne-1870 | half-back | wp-player | https://en.wikipedia.org/wiki/Robert_Milne_(footballer) |
| bootle | andrew-watson-1856 | full-back | wp-player | https://en.wikipedia.org/wiki/Andrew_Watson_(footballer,_born_1856) |
| bootle | job-wilding-1862 | right-wing | wp-player-line | https://en.wikipedia.org/wiki/Job_Wilding |
| bootle | hugh-galbraith-1866 | left-wing | wp-player-line | https://en.wikipedia.org/wiki/Hugh_Galbraith_(footballer) |
| bootle | walter-evans-1867 | full-back | wp-player | https://en.wikipedia.org/wiki/Walter_Evans_(footballer,_born_1867) |
| bootle | david-jardine-1867 | goalkeeper | wp-player | https://en.wikipedia.org/wiki/David_Jardine_(footballer) |
| sunderland-albion | jimmy-hannah-1868 | right-wing | wp-player-line | https://en.wikipedia.org/wiki/Jimmy_Hannah |
| sunderland-albion | john-angus-1867 | goalkeeper | wp-player | https://en.wikipedia.org/wiki/John_Angus_(footballer,_born_1867) |
| crewe-alexandra | william-bell-1860 | half-back | wp-player | https://en.wikipedia.org/wiki/William_Bell_(footballer,_born_1859) |
| crewe-alexandra | john-pearson-1868 | inside-right | wp-player | https://en.wikipedia.org/wiki/John_Pearson_(footballer,_born_1868) |
| crewe-alexandra | alfred-davies-1863 | full-back | wp-player | https://en.wikipedia.org/wiki/Alfred_Owen_Davies |
| old-carthusians | william-cobbold-1863 | inside-right | wp-player-line | https://en.wikipedia.org/wiki/Nevill_Cobbold |
| old-carthusians | edward-hagarty-parry-1855 | inside-left | wp-player-line | https://en.wikipedia.org/wiki/Edward_Hagarty_Parry |
| old-carthusians | maurice-stanbrough-1870 | right-wing | wp-player | https://en.wikipedia.org/wiki/Maurice_Stanbrough |
| old-carthusians | percy-walters-1863 | full-back | wp-player | https://en.wikipedia.org/wiki/Percy_Melmoth_Walters |
| old-carthusians | arthur-walters-1864 | full-back | wp-player | https://en.wikipedia.org/wiki/Arthur_Melmoth_Walters |
| old-carthusians | william-page-1862 | centre-forward | wp-player | https://en.wikipedia.org/wiki/William_Page_(footballer) |
| notts-rangers | arthur-elliott-1870 | right-wing | wp-player-line | https://en.wikipedia.org/wiki/Arthur_Elliott_(footballer) |
| sheffield-wednesday | michael-whitham-1867 | full-back | wp-player | https://en.wikipedia.org/wiki/Michael_Whitham |
| birmingham-st-georges | billy-siddons-1864 | full-back | wp-player | https://en.wikipedia.org/wiki/William_Siddons |
| birmingham-st-georges | jack-devey-1866 | right-wing | wp-player-line | https://en.wikipedia.org/wiki/Jack_Devey |
| small-heath | ted-devey-1871 | half-back | wp-player | https://en.wikipedia.org/wiki/Ted_Devey |
| small-heath | will-devey-1866 | centre-forward | wp-player | https://en.wikipedia.org/wiki/Will_Devey |
| walsall | charlie-shaw-1862 | right-wing | wp-player-line | https://en.wikipedia.org/wiki/Charlie_Shaw_(footballer,_born_1862) |

## Disagreements left alone (sourced entries, for the next review)
- Alf Milward (Everton): `left-wing` per PLAYERS_1888-89.md; the season article lists 6 apps at centre forward.
- Tommy Knight (Wolves): `inside-right` per PLAYERS_1888-89.md; last (outside left) in the 1889 final line-up.
- Henry Cursham (Notts County): `centre-forward` per PLAYERS_1888-89.md; FB in the season article's table.
- Archie Goodall (Villa): FW in the season table, but his article has his Villa debut at centre half.
