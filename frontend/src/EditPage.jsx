import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import bellSvg from "./assets/bell.svg";
import heartSvg from "./assets/heart.svg";
import acornSvg from "./assets/acorn.svg";
import leafSvg from "./assets/leaf.svg";
import { usePlayer } from "./context/PlayerContext";
import CardTableDecoration from "./pageComponents/CardTableDecoration";
import EditButton from "./pageComponents/EditButton";
import EditPageField from "./pageComponents/EditPageField";

function EditPage() {
  const navigate = useNavigate();
  const { player } = usePlayer();

  useEffect(() => {
    if (!player) {
      navigate(`/`);
    }
  }, [player, navigate]);

  const [name, setName] = useState(player?.playerName || "");
  const [email, setEmail] = useState(player?.email || "");
  const [password, setPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState("");

  async function handleDelete() {
    try {
      const response = await fetch(`/api/user/delete`, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("jwtToken")}`,
        },
      });
      if (!response.ok) {
        throw new Error((await response.json()).message);
      }
      setMessage("");
      console.log(`User ${name} has been deleted`);
      navigate(`/`);
    } catch (error) {
      setMessage(error.message);
      console.error("Error deleting user:", error);
    }
  }

  async function handleSave(e) {
    e.preventDefault();
    setMessage("");
    try {
      const token = (() => {
        try {
          return localStorage.getItem("jwtToken");
        } catch {
          return null;
        }
      })();
      const res = await fetch("/api/user/me", {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          ...(token ? { Authorization: "Bearer " + token } : {}),
        },
        body: JSON.stringify({
          playerName: name,
          email,
          password,
          newPassword:
            newPassword && newPassword === confirmPassword ? newPassword : "",
        }),
      });
      const data = await res.json();
      console.log(data);
      if (!res.ok) throw new Error(data.message || "Failed to update profile");
      setMessage(data.message);
      setName("");
      setTimeout(() => navigate("/"), 3000);
    } catch (err) {
      setMessage(err.message);
    }
  }

  function handleCancel() {
    navigate("/menu");
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-table-background px-4 py-8">
      <div className="p-4 sm:p-6 bg-[#4B2E1F] rounded-[90px] shadow-inner w-full max-w-7xl">
        <div className="w-full h-168 bg-poker-table rounded-[70px] shadow-2xl flex flex-col items-center justify-center relative px-6 sm:px-8 dark:text-gray-50">
          
          <CardTableDecoration />
          
          <h1 className="mb-6 text-3xl font-extrabold leading-tight tracking-tight text-gray-900 md:text-4xl xl:text-3xl dark:text-gray-50 text-center">
                Edit your account:
              </h1>
          <div className="text-xl font-bold z-10 w-full bg-white border border-gray-200 rounded-2xl shadow dark:border-gray-700 dark:bg-gray-800 md:mt-0 md:max-w-2/3 2xl:max-w-1/2 xl:p-0">
            <div className="p-6 space-y-4 md:space-y-8 sm:p-8">
              
              <form className="space-y-4" onSubmit={handleSave}>
                <EditPageField
                  htmlFor="username"
                  labelText="Your username"
                  inputType="text"
                  inputName="username"
                  placeholder="username"
                  value={name}
                  onChangeHandler={setName}
                  required={true}
                />
                <EditPageField
                  htmlFor="email"
                  labelText="Email address"
                  inputType="email"
                  inputName="email"
                  placeholder="email"
                  value={email}
                  onChangeHandler={setEmail}
                />
                <EditPageField
                  htmlFor="password"
                  labelText="Your password"
                  inputType="password"
                  inputName="password"
                  placeholder="••••••••"
                  onChangeHandler={setPassword}
                  autoComplete="off"
                  required={true}
                />
                <EditPageField
                  htmlFor="new-password"
                  labelText="New password"
                  inputType="password"
                  inputName="new-password"
                  placeholder="••••••••"
                  onChangeHandler={setNewPassword}
                  autoComplete="off"
                  required={false}
                />
                <EditPageField
                  htmlFor="confirm-password"
                  labelText="Confirm password"
                  inputType="password"
                  inputName="confirm-password"
                  placeholder="••••••••"
                  onChangeHandler={setConfirmPassword}
                  autoComplete="off"
                  required={false}
                />
                <div>
                  {confirmPassword && newPassword !== confirmPassword && (
                    <div>New passwords do not match</div>
                  )}
                </div>
                {message && (
                  <p className="text-sm font-light text-red-500 dark:text-red-400">
                    {message}
                  </p>
                )}
                <EditButton
                  onHandle={handleSave}
                  type="submit"
                  buttonText="Save changes"
                />
                <div>
                  <EditButton
                    onHandle={handleDelete}
                    type="button"
                    buttonText="Delete account"
                  />
                </div>
                <div>
                  <EditButton
                    onHandle={handleCancel}
                    type="button"
                    buttonText="Back to menu"
                  />
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default EditPage;
