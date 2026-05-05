import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import client from '../../api/client';
import type { Approval } from '../../api/types';

export default function AdminApprovalsPage() {
  const [approvals, setApprovals] = useState<Approval[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    client.get('/admin/approvals')
      .then(res => setApprovals(res.data.data))
      .finally(() => setLoading(false));
  }, []);

  const handleApprove = async (id: number) => {
    try {
      await client.post(`/admin/approvals/${id}/approve`, { note: 'Approved' });
      setApprovals(prev => prev.map(a => a.id === id ? { ...a, status: 'APPROVED' as const } : a));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Action failed');
    }
  };

  const handleReject = async (id: number) => {
    const note = prompt('Rejection reason:');
    if (note === null) return;
    try {
      await client.post(`/admin/approvals/${id}/reject`, { note });
      setApprovals(prev => prev.map(a => a.id === id ? { ...a, status: 'REJECTED' as const } : a));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Action failed');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold">Venue Approvals</h1>
          <Link to="/admin" className="text-blue-600 hover:underline">Dashboard</Link>
        </div>
      </header>
      <main className="max-w-3xl mx-auto px-4 py-6">
        {loading ? (
          <p className="text-gray-400 text-center">Loading...</p>
        ) : approvals.length === 0 ? (
          <p className="text-gray-400 text-center">No pending approvals</p>
        ) : (
          <div className="space-y-3">
            {approvals.map(a => (
              <div key={a.id} className="bg-white rounded-lg shadow-sm p-4">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="font-medium">{a.venueName}</h3>
                    <p className="text-sm text-gray-500">Submitted by: {a.submittedByName}</p>
                    <p className="text-sm text-gray-500">
                      Status: <span className={`font-medium ${a.status === 'PENDING' ? 'text-yellow-600' : a.status === 'APPROVED' ? 'text-green-600' : 'text-red-600'}`}>{a.status}</span>
                    </p>
                    {a.reviewNote && <p className="text-sm text-gray-500">Note: {a.reviewNote}</p>}
                  </div>
                  {a.status === 'PENDING' && (
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleApprove(a.id)}
                        className="px-3 py-1 bg-green-600 text-white rounded text-sm hover:bg-green-700"
                      >
                        Approve
                      </button>
                      <button
                        onClick={() => handleReject(a.id)}
                        className="px-3 py-1 bg-red-600 text-white rounded text-sm hover:bg-red-700"
                      >
                        Reject
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
