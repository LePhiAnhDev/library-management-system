import type { NextConfig } from "next"

const nextConfig: NextConfig = {
  // Standalone output: bundles a minimal server + traced dependencies into
  // .next/standalone so the production Docker image can run `node server.js`
  // without the full node_modules tree.
  output: "standalone",
}

export default nextConfig
