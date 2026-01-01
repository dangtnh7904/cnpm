import React from "react";
import { Table } from "antd";

export default function DataTable({
  columns,
  dataSource,
  loading = false,
  rowKey = "id",
  pagination = false,
  ...props
}) {
  return (
    <Table
      rowKey={rowKey}
      columns={columns}
      dataSource={dataSource}
      loading={loading}
      pagination={pagination}
      {...props}
    />
  );
}
