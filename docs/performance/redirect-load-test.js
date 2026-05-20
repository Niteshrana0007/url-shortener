import http from 'k6/http'
import { check, sleep } from 'k6'
import { Rate, Trend } from 'k6/metrics'

// Custom metrics
const redirectErrors = new Rate('redirect_errors')
const redirectLatency = new Trend('redirect_latency_ms', true)

export const options = {
  stages: [
    { duration: '30s', target: 50 },   // Ramp up
    { duration: '2m',  target: 200 },  // Sustained load
    { duration: '30s', target: 500 },  // Spike
    { duration: '1m',  target: 200 },  // Recovery
    { duration: '30s', target: 0 }     // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<50'],    // P95 redirect < 50ms (spec requirement)
    redirect_errors: ['rate<0.01'],     // < 1% error rate
    http_req_failed: ['rate<0.01']
  }
}

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080'
const TEST_ALIAS = __ENV.TEST_ALIAS || 'test-alias'

export function setup() {
  // Verify the test alias exists before load test
  const res = http.get(`${BASE_URL}/api/v1/${TEST_ALIAS}`, {
    redirects: 0
  })
  if (res.status !== 302) {
    throw new Error(`Test alias "${TEST_ALIAS}" not found. Create it first.`)
  }
  return { alias: TEST_ALIAS }
}

export default function (data) {
  const start = Date.now()

  const res = http.get(`${BASE_URL}/api/v1/${data.alias}`, {
    redirects: 0,
    tags: { name: 'redirect' }
  })

  const latency = Date.now() - start
  redirectLatency.add(latency)

  const success = check(res, {
    'status is 302': (r) => r.status === 302,
    'has Location header': (r) => r.headers['Location'] !== undefined,
    'latency < 100ms': () => latency < 100
  })

  redirectErrors.add(!success)
  sleep(0.1)
}

export function handleSummary(data) {
  return {
    'stdout': JSON.stringify({
      p95_ms: data.metrics.redirect_latency_ms?.values?.['p(95)'],
      p99_ms: data.metrics.redirect_latency_ms?.values?.['p(99)'],
      error_rate: data.metrics.redirect_errors?.values?.rate,
      total_requests: data.metrics.http_reqs?.values?.count
    }, null, 2)
  }
}
