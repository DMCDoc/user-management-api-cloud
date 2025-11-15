import { Card, CardContent, Typography } from "@mui/material";
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from "recharts";

export default function StatsChart({ stats }) {
  const data = [
    { name: "Users", value: stats.totalUsers },
    { name: "Admins", value: stats.totalAdmins },
    { name: "Blocked", value: stats.totalBlocked },
  ];

  const colors = ["#2196f3", "#f44336", "#ff9800"];

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" mb={2}>User Distribution</Typography>
        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie
              data={data}
              dataKey="value"
              nameKey="name"
              innerRadius={60}
              outerRadius={100}
              fill="#8884d8"
              paddingAngle={3}
            >
              {data.map((entry, index) => (
                <Cell key={index} fill={colors[index]} />
              ))}
            </Pie>
            <Tooltip />
          </PieChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}
