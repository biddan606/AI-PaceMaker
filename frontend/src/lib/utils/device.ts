/**
 * 브라우저 디바이스 식별자 관리 유틸리티
 *
 * deviceId는 다중 디바이스 로그인을 지원하기 위해 사용됩니다.
 * localStorage에 저장하여 동일 브라우저에서는 같은 deviceId를 사용합니다.
 */

const DEVICE_ID_KEY = 'deviceId';

/**
 * deviceId를 가져오거나 생성합니다.
 * localStorage에 저장된 deviceId가 있으면 반환하고, 없으면 새로 생성합니다.
 *
 * @returns {string} deviceId
 */
export function getOrCreateDeviceId(): string {
	if (typeof window === 'undefined') {
		// SSR 환경에서는 임시 ID 반환 (실제로는 클라이언트에서만 사용됨)
		return 'ssr-temp-device-id';
	}

	let deviceId = localStorage.getItem(DEVICE_ID_KEY);

	if (!deviceId) {
		// crypto.randomUUID()는 modern browsers에서 지원
		deviceId = crypto.randomUUID();
		localStorage.setItem(DEVICE_ID_KEY, deviceId);
	}

	return deviceId;
}

/**
 * 저장된 deviceId를 삭제합니다.
 * (주로 테스트 또는 디버깅 목적으로 사용)
 */
export function clearDeviceId(): void {
	if (typeof window !== 'undefined') {
		localStorage.removeItem(DEVICE_ID_KEY);
	}
}
