import React from "react";
import { Typography } from "antd";
import "./ContentCard.css";

const { Title } = Typography;

export default function ContentCard({ title, extra, children }) {
  return (
    <div className="content-card">
      {(title || extra) && (
        <div className="content-card-header">
          {title && (
            <Title level={4} className="content-card-title">
              {title}
            </Title>
          )}
          {extra && <div className="content-card-extra">{extra}</div>}
        </div>
      )}
      {children}
    </div>
  );
}
