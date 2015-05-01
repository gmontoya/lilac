from collections import OrderedDict
import sys
import ast
import string

def main(argv):
    file=argv[1]
    with open(file, 'r') as f:
        for l in f:
            if (not (l[0] == '{')) or (not (l[-1] == '\n')) or (not (l[-2] == '}')):
                continue
            d = eval(l)
            e = dict()
            for var in d:
                v=d[var]
                p1=string.rfind(v, "^^<")
                if len(v) > 4 and v[-1] == '>' and (p1 > 0):
                    v = v[0:p1]
                if len(v) > 3 and v[-3] == '@':
                    v = v[:-3]
                e[unicode(var)] = unicode(v)
            e = OrderedDict(sorted(e.items(), key=lambda t: t[0]))
            print e

if __name__ == '__main__':
    main(sys.argv)
