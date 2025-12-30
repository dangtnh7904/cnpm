import dayjs from "dayjs";
import { DATE_FORMAT } from "../constants";

export const formatDate = (date, format = DATE_FORMAT) => {
  if (!date) return "";
  return dayjs(date).format(format);
};

export const parseDate = (dateString) => {
  if (!dateString) return null;
  return dayjs(dateString);
};

export const toDatePayload = (date) => {
  if (!date) return null;
  return date.format(DATE_FORMAT);
};
