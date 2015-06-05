package util.math;

import java.util.Map;

import collection.map.LinkedHashMap;

public class Math {

	public static long computePrimeSum(final int range) {
		int i, k;
		Map<Integer, Long> S = new LinkedHashMap<>();
		int r = (int) java.lang.Math.sqrt(range);
		int p = range / r;
		int[] V = new int[r + p - 1];
		k = r + 1;
		for (i = 1; i < k; i++) {
			V[i - 1] = range / i;
		}
		int count = 1;
		for (i = r + p - 2; i >= r; i--) {
			V[i] = count++;
		}
		int vi;
		for (i = 0; i < V.length; i++) {
			vi = V[i];
			S.put(vi, ((long) vi * (vi + 1) / 2 - 1));
		}
		Long sp, p2;
		int r1 = r + 1;
		for (p = 2; p < r1; p++) {
			Long temp1 = S.get(p - 1);
			if (S.get(p) > temp1) {
				sp = temp1;
				p2 = (long) (p * p);
				for (i = 0; i < V.length; i++) {
					vi = V[i];
					if (vi < p2) {
						break;
					}
					S.put(vi, S.get(vi) - p * (S.get(vi / p) - sp));
				}
			}
		}
		return S.get(range);
	}
}