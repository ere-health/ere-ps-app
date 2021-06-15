import pandas as pd


def normalize_pzn(pzn):
    pzn = str(pzn)
    if len(pzn) >= 8:
        return pzn
    padding_size = 8 - len(pzn)
    return '0' * padding_size + pzn


df = pd.read_csv('<input-file>.csv')

df.PZN = df.PZN.apply(normalize_pzn)
df.to_csv('../resources/data/<output-file>.csv', index=None)
