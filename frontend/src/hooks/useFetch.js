import { useState, useEffect, useCallback } from "react";

export default function useFetch(fetchFunction, autoFetch = true) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetch = useCallback(async (...args) => {
    if (!fetchFunction || typeof fetchFunction !== 'function') {
      console.error('useFetch: fetchFunction is not a function', fetchFunction);
      setError(new Error('fetchFunction is not defined'));
      setData([]); // Ensure data is always an array
      return [];
    }
    
    setLoading(true);
    setError(null);
    try {
      const result = await fetchFunction(...args);
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
  }, [fetchFunction]);

  useEffect(() => {
    if (autoFetch && fetchFunction) {
      fetch();
    }
  }, [autoFetch, fetch, fetchFunction]);

  return { data, loading, error, refetch: fetch, setData };
}
