import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import client from '../../api/client';
import type { User } from '../../api/types';

export default function AdminUsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    client.get('/admin/users')
      .then(res => setUsers(res.data.data))
      .finally(() => setLoading(false));
  }, []);

  const handleToggle = async (id: number) => {
    try {
      const res = await client.put(`/admin/users/${id}/toggle`);
      setUsers(prev => prev.map(u => u.id === id ? res.data.data : u));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Action failed');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">User Management</h1>
          <Link to="/admin" className="text-blue-600 hover:underline">Dashboard</Link>
        </div>
      </header>
      <main className="max-w-3xl mx-auto px-4 py-6">
        {loading ? (
          <p className="text-gray-400 text-center">Loading...</p>
        ) : (
          <div className="bg-white rounded-lg shadow-sm overflow-hidden">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Name</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Email</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Role</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Status</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {users.map(user => (
                  <tr key={user.id}>
                    <td className="px-4 py-3 text-sm">{user.name}</td>
                    <td className="px-4 py-3 text-sm">{user.email}</td>
                    <td className="px-4 py-3 text-sm">{user.role.replace('ROLE_', '')}</td>
                    <td className="px-4 py-3 text-sm">
                      <span className={user.active ? 'text-green-600' : 'text-red-600'}>
                        {user.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-sm">
                      <button
                        onClick={() => handleToggle(user.id)}
                        className="text-blue-600 hover:underline"
                      >
                        {user.active ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  );
}
