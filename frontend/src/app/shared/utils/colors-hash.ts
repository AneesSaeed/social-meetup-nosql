/**
 * Maps an arbitrary value (e.g., an interest label) to a stable color bucket.
 * - Normalizes input (stringifies, trims, lowercases) so equivalent values map to the same result.
 * - Builds a simple 32-bit hash by iterating characters and mixing with `h = h * 31 + charCode`.
 * - Uses `>>> 0` to keep the hash as an unsigned 32-bit integer (stable, non-negative, integer-safe).
 * - Returns `h % mod` to reduce the hash into one of `mod` buckets (0..mod-1) for CSS classes.
 */
export function colorBucket(value: unknown, mod = 8): number {
  const s = String(value ?? '').trim().toLowerCase();

  let h = 0;
  for (let i = 0; i < s.length; i++) {
    h = (h * 31 + s.charCodeAt(i)) >>> 0;
  }

  return h % mod;
}
