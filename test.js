import http from 'k6/http';
import { check } from 'k6';

export let options = {
  vus: 1000,              // 1000 concurrent users
  duration: '30s',        // Run for 30 seconds
  thresholds: {
    http_req_duration: ['p(95)<3000'],  // 95% of requests should be below 3s
    http_reqs: ['count>10000'],        // Expect at least 10k requests total
  },
};

export default function () {
  const url = 'http://localhost:8085/invoke';
  const payload = JSON.stringify({
    apiMethod: "POST",
    requestDTO: {
      url: "http://localhost:8083/mock-api/post",
      headerVariables: { "Authorization": "Bearer test" },
      params: [],
      bodyType: "application/json",
      requestBody: "{\"key\": \"value\"}"
    },
    timeout: 10000,
    useSsl: false
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
  };

  const res = http.post(url, payload, params);
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
}
