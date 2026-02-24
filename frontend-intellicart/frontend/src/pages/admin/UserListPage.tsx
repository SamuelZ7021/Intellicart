import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { userApi } from '@/lib/axios';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';

interface User {
    id: number;
    username: string;
    email: string;
    roles: string[];
}

export const UserListPage = () => {
    const navigate = useNavigate();
    const { data: users, isLoading, error } = useQuery<User[]>({
        queryKey: ['users'],
        queryFn: async () => {
            try {
                const response = await userApi.get('/users');
                return response.data;
            } catch (err) {
                console.warn("Using mock users");
                return [
                    { id: 1, username: 'admin', email: 'admin@example.com', roles: ['ADMIN', 'USER'] },
                    { id: 2, username: 'johndoe', email: 'john@example.com', roles: ['USER'] }
                ];
            }
        },
    });

    if (isLoading) {
        return (
            <div className="flex justify-center items-center py-20">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
            </div>
        );
    }

    if (error) {
        return <div className="text-red-500">Error loading users</div>;
    }

    return (
        <div className="container mx-auto py-8">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold">User Management</h1>
                <Button onClick={() => navigate('/admin/users/new')}>Add User</Button>
            </div>

            <div className="border rounded-md">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>ID</TableHead>
                            <TableHead>Username</TableHead>
                            <TableHead>Email</TableHead>
                            <TableHead>Roles</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {users?.map((user) => (
                            <TableRow key={user.id}>
                                <TableCell>{user.id}</TableCell>
                                <TableCell className="font-medium">{user.username}</TableCell>
                                <TableCell>{user.email}</TableCell>
                                <TableCell>{user.roles.join(', ')}</TableCell>
                                <TableCell className="text-right">
                                    <Button variant="outline" size="sm" className="mr-2" onClick={() => navigate(`/admin/users/${user.id}`)}>
                                        View
                                    </Button>
                                    <Button variant="ghost" size="sm" className="mr-2" onClick={() => navigate(`/admin/users/${user.id}/edit`)}>
                                        Edit
                                    </Button>
                                    <Button variant="ghost" size="sm" className="text-red-500">
                                        Delete
                                    </Button>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>
        </div>
    );
};
