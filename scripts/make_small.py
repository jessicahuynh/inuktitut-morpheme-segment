with open('corpus/NunavutHansard-sm.txt','w') as small:
    with open('corpus/NunavutHansard.txt','r',encoding='utf-8') as f:
        NUM_LINES = 100000
        i = 0
        for line in f:
            if i < NUM_LINES:
                small.write(line)
                i += 1
            else:
                break

print("Done shrinking corpus!")