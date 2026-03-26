import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * n<=20, m<=8
 * State S: items in box. Pile is complement.
 * Valid state: both S and (~S) are "safe" (no dangerous pair inside).
 * Move: pick 1 to m items from one side to the other, result must be valid.
 * BFS for shortest steps.
 */

public class q1 {
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

    //全局变量
    static int n,m,k;
    //存储互相反应的信息
    static int[] badMask;

    static boolean[] buildSafe(int n,int[] badMask){
        int N  =1<<n; //一共 2^n 种材料的组合
        boolean[] safe = new boolean[N];
        safe[0] = true;//empty set is safe
        //一个mask表示一种组合
        for(int mask=1;mask<N;mask++){
            //判断safe只用看最后一个1，结合前面的safe验证即可
            int lsb = mask & -mask;//【取最低位的1】
            int i =Integer.numberOfTrailingZeros(lsb);//最低位1是第几个
            int rest = mask^lsb;//按位取xor，得到剩下材料的集合
            safe[mask] = safe[rest] && ((badMask[i] & rest)==0);
        }
        return safe;
    }

    static int doubleBfs(){
        int FULL = (1<<n) -1;
        boolean[] safe = buildSafe(n,badMask);

        boolean[] valid =new boolean[1<<n];
        for (int state = 0; state <= FULL; state++) {
            //items in box and pile should both be safe
            valid[state] = safe[state] && safe[FULL ^ state];
        }
        //无材料，0步
        if(FULL == 0) return 0;

        int N = 1<<n;
        int[] distA =new int[N];
        int[] distB =new int[N];
        Arrays.fill(distA,-1);
        Arrays.fill(distB,-1);

        ArrayDeque<Integer> qa = new ArrayDeque<>();
        ArrayDeque<Integer> qb = new ArrayDeque<>();
        distA[0] = 0;
        qa.add(0);
        distB[FULL] = 0;
        qb.add(FULL);
        //double bfs
        while (!qa.isEmpty() && !qb.isEmpty()) {
            if(qa.size() <= qb.size()) {
                int ans = expand1Layer(qa,distA,distB,valid,FULL);
                if(ans != -1) return ans;
            }else{
                int ans = expand1Layer(qb,distB,distA,valid,FULL);
                if(ans != -1) return ans;
            }
        }
        return -1;
    }

    static int expand1Layer(ArrayDeque<Integer> q, int[] distThis, int[] distOther,boolean[] valid,int FULL){
        int size = q.size();
        while(size-->0){
            int S = q.poll();
            int d = distThis[S];
            int pile = FULL ^ S;

            //枚举所有堆的子集
            for(int T =pile; T!=0; T=(T-1) & pile){
                if(Integer.bitCount(T)>m)continue;
                //把T加入箱子
                int S2 =S | T;
                if(S2!=0 && S2 != FULL && !valid[S2]){continue;}
                //访问过
                if(distThis[S2]!=-1){continue;}

                distThis[S2]=d+1;
                if(distOther[S2]!=-1){return distThis[S2]+distOther[S2];}
                //下一层（下一步）的state
                q.add(S2);
            }
            //枚举所有箱子的子集
            for (int T = S; T !=0 ; T=(T-1) & S) {
                if(Integer.bitCount(T)>m)continue;
                //把T从箱子拿出去
                int S2 =S ^ T;
                if(S2!=0 && S2 != FULL && !valid[S2]){continue;}
                if(distThis[S2]!=-1){continue;}
                distThis[S2]=d+1;
                if(distOther[S2]!=-1){return distThis[S2]+distOther[S2];}
                q.add(S2);
            }
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
            //按位赋值：|=
            badMask[a] |=(1<<b);
            badMask[b] |=(1<<a);
        }
        System.out.print(doubleBfs());
    }
}
