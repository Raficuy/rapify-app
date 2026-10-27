// RapiFy anonymous user counter — Cloudflare Worker + D1.
// Menyimpan hanya: ID acak (UUID) + waktu pertama & terakhir terlihat. Tidak ada IP, nama, atau akun.

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

const json = (data, status = 200, extra = {}) =>
  new Response(JSON.stringify(data), {
    status,
    headers: { "Content-Type": "application/json", ...extra },
  });

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const now = Math.floor(Date.now() / 1000);

    if (request.method === "POST" && url.pathname === "/ping") {
      let id;
      try {
        ({ id } = await request.json());
      } catch {
        return json({ error: "bad request" }, 400);
      }
      if (typeof id !== "string" || !UUID_RE.test(id)) {
        return json({ error: "invalid id" }, 400);
      }
      await env.DB.prepare(
        `INSERT INTO installs (id, first_seen, last_seen) VALUES (?1, ?2, ?2)
         ON CONFLICT(id) DO UPDATE SET last_seen = excluded.last_seen`
      ).bind(id.toLowerCase(), now).run();
      return json({ ok: true });
    }

    if (request.method === "GET" && url.pathname === "/stats") {
      const row = await env.DB.prepare(
        `SELECT COUNT(*) AS total,
                SUM(CASE WHEN last_seen >= ?1 THEN 1 ELSE 0 END) AS active30d
         FROM installs`
      ).bind(now - 30 * 24 * 3600).first();
      return json(
        { total: row.total ?? 0, active30d: row.active30d ?? 0 },
        200,
        { "Cache-Control": "public, max-age=300" }
      );
    }

    return json({ error: "not found" }, 404);
  },
};
