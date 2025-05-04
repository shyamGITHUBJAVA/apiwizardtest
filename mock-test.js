import http from 'k6/http';
import { check } from 'k6';

export let options = {
  vus: 1000,              // Number of concurrent virtual users
  duration: '30s',        // Test duration
  thresholds: {
    http_req_duration: ['p(95)<1000'],  // 95% of requests should complete < 500ms
    http_reqs: ['count>10000'],        // Minimum 10,000 requests expected
  },
};

export default function () {
  const url = 'http://localhost:8083/mock-api/post';
  const payload = JSON.stringify({ key: "value" });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
  });
}
