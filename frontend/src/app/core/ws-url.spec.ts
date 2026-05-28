import { apiUrlToBrokerUrl } from './ws-url';

describe('apiUrlToBrokerUrl', () => {
  it('converts http API URL to ws broker URL', () => {
    expect(apiUrlToBrokerUrl('http://localhost:8081')).toBe('ws://localhost:8081/ws');
  });

  it('converts https API URL to wss broker URL', () => {
    expect(apiUrlToBrokerUrl('https://api.example.com')).toBe('wss://api.example.com/ws');
  });

  it('strips trailing slash from API URL', () => {
    expect(apiUrlToBrokerUrl('http://localhost:8081/')).toBe('ws://localhost:8081/ws');
  });
});
