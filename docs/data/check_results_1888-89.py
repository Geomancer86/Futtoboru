"""Recompute the 1888-89 Football League table from the real results and compare it with the published one.

Usage: python docs/data/check_results_1888-89.py [path/to/results.json | path/to/results_1888-89.csv]
Default: the game data file gamedata/core/seasons/1888-89/results.json (E-00b, written from the CSV by
tools/data/results_csv_to_json.py); the research CSV is still accepted.
Rules of the season: 2 points a win, 1 a draw, ties on points split by goal average (F / A).
Exit code 0 when every club's P W D L F A Pts and the final order match, 1 otherwise.
"""
import collections, csv, json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
DEFAULT = os.path.join(HERE, '..', '..', 'futtoboru-core', 'src', 'main', 'resources', 'gamedata', 'core', 'seasons',
                       '1888-89', 'results.json')

# Published final table (P, W, D, L, F, A, Pts): Wikipedia "1888-89 Football League" and Steve's Football Stats.
# RSSSF's table has Wolves 50-37 and Everton 35-46 because its grid has Wolves 4-0 Everton (we use 5-0).
PUBLISHED = [
    ('preston-north-end',       (22, 18, 4, 0, 74, 15, 40)),
    ('aston-villa',             (22, 12, 5, 5, 61, 43, 29)),
    ('wolverhampton-wanderers', (22, 12, 4, 6, 51, 37, 28)),
    ('blackburn-rovers',        (22, 10, 6, 6, 66, 45, 26)),
    ('bolton-wanderers',        (22, 10, 2, 10, 63, 59, 22)),
    ('west-bromwich-albion',    (22, 10, 2, 10, 40, 46, 22)),
    ('accrington',              (22, 6, 8, 8, 48, 48, 20)),
    ('everton',                 (22, 9, 2, 11, 35, 47, 20)),
    ('burnley',                 (22, 7, 3, 12, 42, 62, 17)),
    ('derby-county',            (22, 7, 2, 13, 41, 61, 16)),
    ('notts-county',            (22, 5, 2, 15, 40, 73, 12)),
    ('stoke',                   (22, 4, 4, 14, 26, 51, 12)),
]


def read_rows(path):
    """League rows as CSV-style dicts (home_id, away_id, home_goals, away_goals) from the JSON or the CSV."""
    with open(path, encoding='utf-8') as f:
        if path.endswith('.json'):
            return [{'home_id': r['home'], 'away_id': r['away'], 'home_goals': r['homeGoals'],
                     'away_goals': r['awayGoals']}
                    for r in json.load(f) if r['edition'] == 'football-league-1888-89']
        return list(csv.DictReader(f))


def main():
    path = sys.argv[1] if len(sys.argv) > 1 else DEFAULT
    tot = collections.defaultdict(lambda: [0] * 7)
    home = collections.defaultdict(lambda: [0] * 5)
    pairs = collections.Counter()
    rows = read_rows(path)
    for r in rows:
        h, a, hg, ag = r['home_id'], r['away_id'], int(r['home_goals']), int(r['away_goals'])
        pairs[(h, a)] += 1
        for club, gf, ga, at_home in ((h, hg, ag, True), (a, ag, hg, False)):
            k = 1 if gf > ga else 2 if gf == ga else 3
            s = tot[club]
            s[0] += 1; s[k] += 1; s[4] += gf; s[5] += ga; s[6] += (2, 1, 0)[k - 1]
            if at_home:
                hs = home[club]; hs[k - 1] += 1; hs[3] += gf; hs[4] += ga
    ok = len(rows) == 132 and all(n == 1 for n in pairs.values()) and len(pairs) == 132
    print('matches: %d, distinct home/away pairs: %d' % (len(rows), len(pairs)))
    order = sorted(tot, key=lambda c: (-tot[c][6], -tot[c][4] / max(tot[c][5], 1)))
    pub = dict(PUBLISHED)
    print('pos club                     P  W  D  L  F  A Pts | home W D L  F  A')
    for i, c in enumerate(order, 1):
        s = tuple(tot[c]); bad = s != pub.get(c)
        ok &= not bad
        print('%2d  %-23s %s | %s%s' % (i, c, ' '.join('%2d' % v for v in s), ' '.join('%2d' % v for v in home[c]),
                                         '  <-- published %s' % (pub.get(c),) if bad else ''))
    same_order = order == [c for c, _ in PUBLISHED]
    print('final order matches published table:', same_order)
    ok &= same_order
    print('OK' if ok else 'MISMATCH')
    return 0 if ok else 1


if __name__ == '__main__':
    sys.exit(main())
