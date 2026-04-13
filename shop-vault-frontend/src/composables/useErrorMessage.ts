interface ErrorLike {
  response?: {
    data?: {
      msg?: string;
      message?: string;
    };
  };
  message?: string;
}

export function extractErrorMessage(error: unknown, fallback = '操作失败'): string {
  if (!error) return fallback;

  const err = error as ErrorLike;

  if (err.response?.data?.msg) {
    return err.response.data.msg;
  }

  if (err.response?.data?.message) {
    return err.response.data.message;
  }

  if (err.message) {
    return err.message;
  }

  if (typeof error === 'string') {
    return error;
  }

  return fallback;
}
