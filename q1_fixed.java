import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * Fixed solution for OJ1.
 *
 * Bugs fixed from q1.java:
 * 1. State now includes Ilya's position as bit n (0=pile, 1=box).
 * 2. Validity check is side-aware:
 *      side=0 (Ilya at pile): box must be safe → safe[S]
 *      side=1 (Ilya at box): pile must be safe → safe[~S & FULL]
 * 3. Subset enumeration now includes T=0 (Ilya can cross without carrying anything).
 * 4. Transitions are side-aware:
 *      side=0: take T ⊆ pile, move to (S|T, 1), valid if safe(pile \ T)
 *      side=1: take T ⊆ box, move to (S\T, 0), valid if safe(S \ T) = safe(S^T)
 *
 * Uses bidirectional BFS:
 *   Forward from initState   = 0              (all in pile, Ilya at pile)
 *   Backward from targetState = FULL | (1<<n) (all in box,  Ilya at box)
 */
public class q1_fixed {

    static class FastScanner {
        private final InputStream in;
        private final byte[] buffer = new byte[1 << 16];
        private int ptr = 0, len = 0;
        FastScanner(InputStream in) { this.in = in; }
        private int readByte() throws IOException {
            if (ptr >= len) {
                len = in.read(buffer);
                ptr = 0;
                if (len <= 0) return -1;
            }
            return buffer[ptr++];
        }
        int nextInt() throws IOException {
            int c;
            do { c = readByte(); } while (c <= ' ' && c != -1);
            int sgn = 1;
            if (c == '-') { sgn = -1; c = readByte(); }
            int x = 0;
            while (c > ' ') {
                x = x * 10 + (c - '0');
                c = readByte();
            }
            return x * sgn;
        }
    }

    static int n, m, k;
    static int[] badMask;

    static boolean[] buildSafe(int n, int[] badMask) {
        int N = 1 << n;
        boolean[] safe = new boolean[N];
        safe[0] = true;
        for (int mask = 1; mask < N; mask++) {
            int lsb = mask & -mask;
            int i = Integer.numberOfTrailingZeros(lsb);
            int rest = mask ^ lsb;
            safe[mask] = safe[rest] && ((badMask[i] & rest) == 0);
        }
        return safe;
    }

    // Shared state for expandLayer (avoids passing many parameters)
    static boolean[] safe;
    static int FULL, SIDE;
    static int[] distA, distB;

    /**
     * Expands one full BFS layer from queue q.
     * distThis is updated with distances for newly discovered states.
     * distOther is read-only to detect intersections with the opposite BFS.
     * Returns the minimum sum (distThis + distOther) over any intersection found,
     * or -1 if no intersection was found in this layer.
     */
    static int expandLayer(ArrayDeque<Integer> q, int[] distThis, int[] distOther) {
        int size = q.size();
        int best = -1;
        while (size-- > 0) {
            int state = q.poll();
            int mat  = state & FULL;
            int side = (state & SIDE) != 0 ? 1 : 0;
            int d    = distThis[state];

            // Determine movable materials based on Ilya's current position
            int movable = (side == 0) ? ((~mat) & FULL) : mat;

            // Enumerate all subsets of movable (including empty set T=0)
            for (int T = movable; ; T = (T - 1) & movable) {
                if (Integer.bitCount(T) <= m) {
                    int newMat, newState;
                    boolean valid;
                    if (side == 0) {
                        // Ilya moves pile→box carrying T
                        newMat   = mat | T;
                        newState = newMat | SIDE;
                        // Pile left behind (pile \ T) must be safe during transit
                        valid = safe[(~newMat) & FULL];
                    } else {
                        // Ilya moves box→pile carrying T
                        newMat   = mat ^ T;
                        newState = newMat; // side bit = 0
                        // Box left behind (box \ T) must be safe during transit
                        valid = safe[newMat];
                    }

                    if (valid && distThis[newState] == -1) {
                        distThis[newState] = d + 1;
                        if (distOther[newState] != -1) {
                            int total = distThis[newState] + distOther[newState];
                            best = (best == -1) ? total : Math.min(best, total);
                        }
                        q.add(newState);
                    }
                }
                if (T == 0) break;
            }
        }
        return best;
    }

    static int biBfs() {
        if (n == 0) return 0;

        FULL = (1 << n) - 1;
        SIDE = 1 << n;
        safe = buildSafe(n, badMask);

        int TOTAL      = SIDE << 1;        // 2 * 2^n states
        int initState  = 0;                // all in pile, Ilya at pile
        int targetState = FULL | SIDE;     // all in box,  Ilya at box

        distA = new int[TOTAL];
        distB = new int[TOTAL];
        Arrays.fill(distA, -1);
        Arrays.fill(distB, -1);

        distA[initState]   = 0;
        distB[targetState] = 0;

        if (initState == targetState) return 0;

        ArrayDeque<Integer> qa = new ArrayDeque<>();
        ArrayDeque<Integer> qb = new ArrayDeque<>();
        qa.add(initState);
        qb.add(targetState);

        while (!qa.isEmpty() || !qb.isEmpty()) {
            int ans;
            // Always expand the smaller frontier to minimise the search space
            if (!qa.isEmpty() && (qb.isEmpty() || qa.size() <= qb.size())) {
                ans = expandLayer(qa, distA, distB);
            } else {
                ans = expandLayer(qb, distB, distA);
            }
            if (ans != -1) return ans;
        }
        return -1;
    }

    public static void main(String[] args) throws IOException {
        FastScanner fs = new FastScanner(System.in);
        n = fs.nextInt();
        m = fs.nextInt();
        k = fs.nextInt();
        badMask = new int[n];
        for (int i = 0; i < k; i++) {
            int a = fs.nextInt();
            int b = fs.nextInt();
            badMask[a] |= (1 << b);
            badMask[b] |= (1 << a);
        }
        System.out.print(biBfs());
    }
}
