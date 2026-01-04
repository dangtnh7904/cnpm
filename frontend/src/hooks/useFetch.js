import { useState, useEffect, useCallback, useRef } from "react";

export default function useFetch(fetchFunction, autoFetch = true) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Dùng ref để lưu fetchFunction, tránh tạo mới callback mỗi lần render
  const fetchFunctionRef = useRef(fetchFunction);
  fetchFunctionRef.current = fetchFunction;

  const fetch = useCallback(async (...args) => {
    const fn = fetchFunctionRef.current;
    if (!fn || typeof fn !== 'function') {
      console.error('useFetch: fetchFunction is not a function', fn);
      setError(new Error('fetchFunction is not defined'));
      setData([]); // Ensure data is always an array
      return [];
    }
    
    setLoading(true);
    setError(null);
    try {
      const result = await fn(...args);
      // Ensure result is always an array if it's expected to be a list
      const normalizedData = Array.isArray(result) ? result : [];
      setData(normalizedData);
      return normalizedData;
    } catch (err) {
      console.error('useFetch error:', err);
      setError(err);
      setData([]); // Reset to empty array on error
      return [];
    } finally {
      setLoading(false);
    }
  }, []); // Không còn dependency vào fetchFunction

  // autoFetch chỉ chạy 1 lần khi mount
  const autoFetchedRef = useRef(false);
  useEffect(() => {
    if (autoFetch && fetchFunctionRef.current && !autoFetchedRef.current) {
      autoFetchedRef.current = true;
      fetch();
    }
  }, [autoFetch, fetch]);

  return { data, loading, error, refetch: fetch, setData };
}
